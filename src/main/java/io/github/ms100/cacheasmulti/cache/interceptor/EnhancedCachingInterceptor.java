package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.expression.EvaluationException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * AOP方法拦截器{@link CacheInterceptor}的增强，在原本的拦截处理里增加了{@link CacheAsMulti @CacheAsMulti}注解的处理
 *
 * @author zhumengshuai
 */
@Slf4j
public class EnhancedCachingInterceptor extends CacheInterceptor {

    private final ConcurrentMap<CacheResolver, EnhancedCacheResolver> enhancedCacheResolverCache = new ConcurrentHashMap<>(1024);

    private static final Field CONTEXT_CONDITION_PASSING_FIELD;

    static {
        CONTEXT_CONDITION_PASSING_FIELD = Objects.requireNonNull(
                ReflectionUtils.findField(CacheAsMultiOperationContext.class, "conditionPassing"));

        CONTEXT_CONDITION_PASSING_FIELD.setAccessible(true);
    }

    @Override
    @Nullable
    public Object invoke(final MethodInvocation invocation) throws Throwable {

        Assert.state(invocation instanceof ProxyMethodInvocation,
                "Invocation must be ProxyMethodInvocation");
        CacheAsMultiOperationInvoker invoker = new CacheAsMultiOperationInvoker((ProxyMethodInvocation) invocation);

        Method method = invocation.getMethod();
        Object target = invocation.getThis();
        Assert.state(target != null, "Target must not be null");
        try {
            return execute(invoker, target, method, invocation.getArguments());
        } catch (CacheOperationInvoker.ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }

    @Override
    @Nullable
    protected Object execute(CacheOperationInvoker invoker, Object target, Method method, Object[] args) {

        EnhancedCachingOperationSource cacheOperationSource = (EnhancedCachingOperationSource) getCacheOperationSource();

        if (cacheOperationSource != null) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
            Collection<CacheAsMultiOperation<?>> multiOperations = cacheOperationSource
                    .getCacheAsMultiOperations(method, targetClass);

            if (!CollectionUtils.isEmpty(multiOperations)) {
                CacheAsMultiOperationContexts contexts = new CacheAsMultiOperationContexts(multiOperations, method, args, target, targetClass);
                return execute(invoker, contexts);
            }
        }

        return super.execute(invoker, target, method, args);
    }

    @Nullable
    private Object execute(final CacheOperationInvoker invoker, CacheAsMultiOperationContexts contexts) {
        // 同步调用的特殊处理
        if (contexts.isSynchronized()) {
            try {
                return Objects.requireNonNull(findCachedItems(contexts, invoker)).getRight();
            } catch (Cache.ValueRetrievalException ex) {
                ReflectionUtils.rethrowRuntimeException(ex.getCause());
            }
        }

        Collection<?> cacheAsMultiArg = contexts.getCacheAsMultiArg();
        // 如果@CacheAsMulti注解的参数值为null或者空集合，则调用原方法返回
        if (CollectionUtils.isEmpty(cacheAsMultiArg)) {
            return invokeOperation(invoker);
        }

        // 处理@CacheEvict的isBeforeInvocation为true的情况
        processCacheEvicts(contexts, true, null);

        Map<?, ?> argValueMap;
        Object returnValue;

        // 如果有需要执行的CachePut
        if (hasCachePut(contexts)) {
            returnValue = invokeOperation(invoker);

            CacheAsMultiOperation<?> multiOperation = contexts.getFirst(CachePutOperation.class).getMultiOperation();
            argValueMap = multiOperation.makeCacheMap(cacheAsMultiArg, returnValue);

            if (!CollectionUtils.isEmpty(argValueMap)) {
                putCachedItems(contexts.get(CacheableOperation.class), argValueMap);
                putCachedItems(contexts.get(CachePutOperation.class), argValueMap);
            }
        } else {
            Pair<Map<?, ?>, Object> pair = findCachedItems(contexts, invoker);
            // 如果存在Cacheable
            if (pair != null) {
                // findCachedItems会执行invoker，所以这个分支里不能有invoker
                argValueMap = pair.getLeft();
                returnValue = pair.getRight();
            }//如果没有Cacheable，那只剩下CacheEvict
            else {
                returnValue = invokeOperation(invoker);
                CacheAsMultiOperation<?> multiOperation = contexts.getFirst(CacheEvictOperation.class).getMultiOperation();
                argValueMap = multiOperation.makeCacheMap(cacheAsMultiArg, returnValue);
            }
        }

        processCacheEvicts(contexts, false, argValueMap);


        return returnValue;
    }

    private void processCacheEvicts(
            CacheAsMultiOperationContexts contexts, boolean beforeInvocation, @Nullable Map<?, ?> argValueMap) {

        Collection<?> cacheAsMultiArg = contexts.getCacheAsMultiArg();
        assert cacheAsMultiArg != null;
        Collection<CacheAsMultiOperationContext> cacheEvictContexts = contexts.get(CacheEvictOperation.class);

        for (CacheAsMultiOperationContext context : cacheEvictContexts) {
            CacheEvictOperation operation = (CacheEvictOperation) context.getOperation();
            if (beforeInvocation != operation.isBeforeInvocation()) {
                continue;
            }
            Pair<Collection<?>, Collection<?>> pair = splitIsConditionPassing(context, cacheAsMultiArg, argValueMap);
            if (pair.getLeft().isEmpty()) {
                continue;
            }
            performCacheEvict(context, operation, pair.getLeft(), argValueMap);
        }
    }

    private void performCacheEvict(
            CacheAsMultiOperationContext context, CacheEvictOperation operation,
            Collection<?> subCacheAsMultiArg, @Nullable Map<?, ?> argValueMap) {

        Collection<Object> keys = null;

        for (EnhancedCache cache : context.getCaches()) {
            if (operation.isCacheWide()) {
                if (log.isTraceEnabled()) {
                    log.trace("Invalidating entire cache for operation " + operation);
                }
                doClear(cache, operation.isBeforeInvocation());
            } else {
                if (keys == null) {
                    keys = generateKeys(context, subCacheAsMultiArg, argValueMap);
                    if (log.isTraceEnabled()) {
                        log.trace("Invalidating cache keys " + keys + " for operation " + operation);
                    }
                }

                doMultiEvict(cache, keys);
            }
        }
    }

    @SneakyThrows
    @Nullable
    private Pair<Map<?, ?>, Object> findCachedItems(CacheAsMultiOperationContexts contexts, CacheOperationInvoker invoker) {

        Collection<CacheAsMultiOperationContext> cacheableContexts = contexts.get(CacheableOperation.class);
        if (cacheableContexts.isEmpty()) {
            return null;
        }

        CacheAsMultiOperationContext firstContext = cacheableContexts.iterator().next();
        CacheAsMultiOperation<?> multiOperation = firstContext.getMultiOperation();
        Collection<?> cacheAsMultiArg = contexts.getCacheAsMultiArg();
        assert cacheAsMultiArg != null;
        Map<Object, Object> argValueMap = CollectionUtils.newHashMap(cacheAsMultiArg.size());

        Collection<?> missCacheAsMultiArg = cacheAsMultiArg;

        for (CacheAsMultiOperationContext context : cacheableContexts) {
            missCacheAsMultiArg = findInCaches(context, missCacheAsMultiArg, argValueMap);

            if (missCacheAsMultiArg.isEmpty()) {
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Hit cache args " + argValueMap.keySet());
        }

        if (missCacheAsMultiArg.isEmpty()) {
            return Pair.of(argValueMap, multiOperation.makeReturnObject(cacheAsMultiArg, argValueMap));
        }

        if (argValueMap.isEmpty()) {
            missCacheAsMultiArg = cacheAsMultiArg;
        } else {
            missCacheAsMultiArg = multiOperation.newCacheAsMultiArg(missCacheAsMultiArg);
        }

        if (log.isDebugEnabled()) {
            log.debug("Miss cache args " + missCacheAsMultiArg);
        }
        // 查询 miss 的结果
        return invokeWithMissCacheAsMultiArg(cacheableContexts, invoker, missCacheAsMultiArg, argValueMap);
    }

    private Collection<?> findInCaches(CacheAsMultiOperationContext context,
                                       Collection<?> subCacheAsMultiArg, Map<Object, Object> argValueMap) {

        Pair<Collection<?>, Collection<?>> pair = splitIsConditionPassing(context, subCacheAsMultiArg, null);
        if (pair.getLeft().isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("No cache entry for cacheAsMulti '" + subCacheAsMultiArg + "' in cache(s) " + context.getCacheNames());
            }

            return subCacheAsMultiArg;
        }

        Map<Object, Object> argKeyMap = generateArgKeyMap(context, pair.getLeft());
        if (log.isTraceEnabled()) {
            log.trace("Find keys " + argKeyMap.values() + " for operation " + context.getOperation());
        }

        Collection<?> missCacheAsMultiArg = pair.getLeft();
        Collection<Object> missKeys = new ArrayList<>(argKeyMap.values());

        for (EnhancedCache cache : context.getCaches()) {
            Map<Object, ValueWrapper> hitKeyValueWrapperMap = doMultiGet(cache, missKeys);
            if (CollectionUtils.isEmpty(hitKeyValueWrapperMap)) {
                continue;
            }

            missKeys.clear();
            Collection<Object> newMissCacheAsMultiArg = new ArrayList<>(missCacheAsMultiArg.size());
            missCacheAsMultiArg.forEach(argItem -> {
                Object key = argKeyMap.get(argItem);
                ValueWrapper valueWrapper = hitKeyValueWrapperMap.get(key);
                if (valueWrapper != null) {
                    argValueMap.put(argItem, valueWrapper.get());
                } else {
                    newMissCacheAsMultiArg.add(argItem);
                    missKeys.add(key);
                }
            });
            missCacheAsMultiArg = newMissCacheAsMultiArg;

            if (log.isTraceEnabled()) {
                Set<?> hitKeys = hitKeyValueWrapperMap.entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect(Collectors.toSet());
                log.trace("Cache entry for key '" + hitKeys + "' found in cache '" + cache.getName() + "'");
            }

            // 如果可以走缓存的都命中了，那直接返回不能走缓存的Collection
            if (missCacheAsMultiArg.isEmpty()) {
                return pair.getRight();
            }
        }

        if (pair.getRight().isEmpty()) {
            return missCacheAsMultiArg;
        }

        // pair的容量更大，所以放到pair中
        ((Collection<Object>) pair.getRight()).addAll(missCacheAsMultiArg);
        return pair.getRight();
    }

    private Pair<Map<?, ?>, Object> invokeWithMissCacheAsMultiArg(
            Collection<CacheAsMultiOperationContext> contexts, CacheOperationInvoker invoker,
            Collection<?> missCacheAsMultiArg, Map<Object, Object> argValueMap) {

        CacheAsMultiOperationContext firstContext = contexts.iterator().next();
        Object invokeValues = invokeOperation(firstContext, invoker, missCacheAsMultiArg);

        // 如果执行结果为null，缓存也没有任何命中，直接返回null
        if (invokeValues == null && argValueMap.size() == 0) {
            return Pair.of(argValueMap, null);
        }

        CacheAsMultiOperation<?> multiOperation = firstContext.getMultiOperation();
        Map<?, ?> missArgValueMap = multiOperation.makeCacheMap(missCacheAsMultiArg, invokeValues);

        // 如果invokeValues是null或者空map，那missArgValueMap也是null或者空map
        if (!CollectionUtils.isEmpty(missArgValueMap)) {
            // 缓存数据
            putCachedItems(contexts, missArgValueMap);

            // 如果缓存都未命中，直接返回执行结果
            if (argValueMap.size() == 0) {
                return Pair.of(missArgValueMap, invokeValues);
            }

            argValueMap.putAll(missArgValueMap);
        }

        return Pair.of(argValueMap, multiOperation.makeReturnObject(firstContext.getCacheAsMultiArg(), argValueMap));
    }

    private void putCachedItems(Collection<CacheAsMultiOperationContext> contexts, Map<?, ?> argValueMap) {
        for (CacheAsMultiOperationContext context : contexts) {
            Pair<Collection<?>, Collection<?>> pair = splitIsConditionPassing(context, argValueMap.keySet(), argValueMap);
            if (pair.getLeft().isEmpty()) {
                continue;
            }

            pair = splitCanPutToCache(context, pair.getLeft(), argValueMap);
            if (pair.getLeft().isEmpty()) {
                continue;
            }

            Map<Object, Object> keyValueMap = generateKeyValueMap(context, pair.getLeft(), argValueMap);
            if (log.isTraceEnabled()) {
                log.trace("Store key-value map " + keyValueMap + " for operation " + context.getOperation());
            }

            for (EnhancedCache cache : context.getCaches()) {
                doMultiPut(cache, keyValueMap);
            }
        }
    }

    private boolean hasCachePut(CacheAsMultiOperationContexts contexts) {

        Collection<?> cacheAsMultiArg = contexts.getCacheAsMultiArg();
        assert cacheAsMultiArg != null;
        Collection<CacheAsMultiOperationContext> cachePutContexts = contexts.get(CachePutOperation.class);
        for (CacheAsMultiOperationContext context : cachePutContexts) {
            try {
                Pair<Collection<?>, Collection<?>> pair = splitIsConditionPassing(context, cacheAsMultiArg, null);
                if (!pair.getLeft().isEmpty()) {
                    return true;
                }
            } catch (EvaluationException ex) {
                if ("org.springframework.cache.interceptor.VariableNotAvailableException".equals(ex.getClass().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private Object invokeOperation(CacheAsMultiOperationContext context,
                                   CacheOperationInvoker invoker, Object missCacheAsMultiArg) {

        Object[] invokeArg = context.getInvokeArg(missCacheAsMultiArg);

        return ((CacheAsMultiOperationInvoker) invoker).invoke(invokeArg);
    }

    private static Pair<Collection<?>, Collection<?>> splitIsConditionPassing(
            CacheAsMultiOperationContext context, Collection<?> subCacheAsMultiArg, @Nullable Map<?, ?> argValueMap) {

        Pair<Collection<?>, Collection<?>> pair = context.splitIsConditionPassing(subCacheAsMultiArg, argValueMap);
        if (log.isTraceEnabled()) {
            log.trace("Cache condition allow:" + pair.getLeft() + ", deny:" + pair.getRight() + " for operation " + context.getOperation());
        }
        return pair;
    }

    private static Pair<Collection<?>, Collection<?>> splitCanPutToCache(
            CacheAsMultiOperationContext context, Collection<?> subCacheAsMultiArg, Map<?, ?> argValueMap) {

        Pair<Collection<?>, Collection<?>> pair = context.splitCanPutToCache(subCacheAsMultiArg, argValueMap);
        if (log.isTraceEnabled()) {
            log.trace("Cache condition allow:" + pair.getLeft() + ", deny:" + pair.getRight() + " for operation " + context.getOperation());
        }
        return pair;
    }

    private Collection<Object> generateKeys(CacheAsMultiOperationContext context,
                                            Collection<?> subCacheAsMultiArg, @Nullable Map<?, ?> argValueMap) {

        if (argValueMap == null) {
            argValueMap = Collections.emptyMap();
        }
        Collection<Object> keys = new ArrayList<>(subCacheAsMultiArg.size());

        for (Object argItem : subCacheAsMultiArg) {
            keys.add(context.generateKey(argItem, argValueMap.get(argItem)));
        }

        return keys;
    }

    private Map<Object, Object> generateArgKeyMap(CacheAsMultiOperationContext context,
                                                  Collection<?> subCacheAsMultiArg) {

        HashMap<Object, Object> argKeyMap = CollectionUtils.newHashMap(subCacheAsMultiArg.size());
        for (Object argItem : subCacheAsMultiArg) {
            argKeyMap.put(argItem, context.generateKey(argItem, null));
        }

        return argKeyMap;
    }

    private Map<Object, Object> generateKeyValueMap(CacheAsMultiOperationContext context,
                                                    Collection<?> subCacheAsMultiArg, Map<?, ?> argValueMap) {

        HashMap<Object, Object> keyValueMap = CollectionUtils.newHashMap(argValueMap.size());
        for (Object argItem : subCacheAsMultiArg) {
            Object value = argValueMap.get(argItem);
            keyValueMap.put(context.generateKey(argItem, value), value);
        }

        return keyValueMap;
    }

    @Nullable
    protected Map<Object, ValueWrapper> doMultiGet(EnhancedCache cache, Collection<?> keys) {
        try {
            return cache.multiGet(keys);
        } catch (RuntimeException ex) {
            getErrorHandler().handleCacheGetError(ex, cache, keys);
            return null;
        }
    }

    protected void doMultiPut(EnhancedCache cache, Map<?, ?> map) {
        try {
            cache.multiPut(map);
        } catch (RuntimeException ex) {
            getErrorHandler().handleCachePutError(ex, cache, map.keySet(), map.values());
        }
    }

    protected void doMultiEvict(EnhancedCache cache, Collection<?> keys) {
        try {
            cache.multiEvict(keys);
        } catch (RuntimeException ex) {
            getErrorHandler().handleCacheEvictError(ex, cache, keys);
        }
    }

    protected CacheAsMultiOperationContext getMultiOperationContext(
            CacheAsMultiOperation<?> multiOperation, Method method,
            Object[] args, Object target, Class<?> targetClass) {

        CacheOperationMetadata metadata = getCacheOperationMetadata(multiOperation.getOperation(), method, targetClass);
        return new CacheAsMultiOperationContext(multiOperation, metadata, args, target);
    }

    @Override
    protected Collection<? extends Cache> getCaches(
            CacheOperationInvocationContext<CacheOperation> context, CacheResolver cacheResolver) {

        if (!(cacheResolver instanceof EnhancedCacheResolver)) {
            cacheResolver = enhancedCacheResolverCache.computeIfAbsent(cacheResolver,
                    resolver -> new EnhancedCachingResolverAdapter(resolver, getBean(
                            Introspector.decapitalize(EnhancedCacheConversionService.class.getSimpleName()),
                            EnhancedCacheConversionService.class)));
        }

        return super.getCaches(context, cacheResolver);
    }

    class CacheAsMultiOperationContexts {

        private final MultiValueMap<Class<? extends CacheOperation>, CacheAsMultiOperationContext> contexts;

        private final boolean sync;

        @Getter
        @Nullable
        private final Collection<?> cacheAsMultiArg;

        public CacheAsMultiOperationContexts(Collection<? extends CacheAsMultiOperation<?>> multiOperations,
                                             Method method, Object[] args, Object target, Class<?> targetClass) {
            /**
             * args数组是{@link MethodInvocation#getArguments()}的引用
             * 改变了它就改变了{@link MethodInvocation#proceed()}的参数
             * 因为计算key的时候需要改变它，为了防止影响所以这里克隆下
             */
            Object[] argsClone = args.clone();
            contexts = new LinkedMultiValueMap<>(multiOperations.size());
            for (CacheAsMultiOperation<?> multiOperation : multiOperations) {
                contexts.add(multiOperation.getOperation().getClass(),
                        getMultiOperationContext(multiOperation, method, argsClone, target, targetClass));
            }
            int cacheAsMultiParameterPosition = multiOperations.iterator().next().getCacheAsMultiParameterPosition();
            cacheAsMultiArg = (Collection<?>) args[cacheAsMultiParameterPosition];
            sync = determineSyncFlag(method);
        }

        public Collection<CacheAsMultiOperationContext> get(Class<? extends CacheOperation> operationClass) {
            Collection<CacheAsMultiOperationContext> result = contexts.get(operationClass);

            return (result != null ? result : Collections.emptyList());
        }

        public CacheAsMultiOperationContext getFirst(Class<? extends CacheOperation> operationClass) {
            return Objects.requireNonNull(contexts.getFirst(operationClass));
        }

        public boolean isSynchronized() {
            return sync;
        }

        private boolean determineSyncFlag(Method method) {
            List<CacheAsMultiOperationContext> cacheOperationContexts = contexts.get(CacheableOperation.class);
            if (cacheOperationContexts == null) {  // no @Cacheable operation at all
                return false;
            }
            boolean syncEnabled = false;
            for (CacheAsMultiOperationContext cacheOperationContext : cacheOperationContexts) {
                if (((CacheableOperation) cacheOperationContext.getOperation()).isSync()) {
                    syncEnabled = true;
                    break;
                }
            }
            if (syncEnabled) {
                if (contexts.size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) cannot be combined with other cache operations on '" + method + "'");
                }
                if (cacheOperationContexts.size() > 1) {
                    throw new IllegalStateException(
                            "Only one @Cacheable(sync=true) entry is allowed on '" + method + "'");
                }
                CacheAsMultiOperationContext cacheOperationContext = cacheOperationContexts.iterator().next();
                CacheableOperation operation = (CacheableOperation) cacheOperationContext.getOperation();
                if (cacheOperationContext.getCaches().size() > 1) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) only allows a single cache on '" + operation + "'");
                }
                if (StringUtils.hasText(operation.getUnless())) {
                    throw new IllegalStateException(
                            "@Cacheable(sync=true) does not support unless attribute on '" + operation + "'");
                }
                return true;
            }
            return false;
        }
    }


    class CacheAsMultiOperationContext extends CacheOperationContext implements CacheOperationInvocationContext<CacheOperation> {

        @Getter
        private final CacheAsMultiOperation<?> multiOperation;

        /**
         * 父类的args字段的引用，改变它以生成缓存key
         */
        private final Object[] superArgs;

        @Getter
        private final Collection<?> cacheAsMultiArg;

        @Nullable
        private Boolean isConditionAllPassing;

        private final Map<Object, Boolean> isConditionPassingCache;

        private final Map<Object, Object> keyCache;

        public CacheAsMultiOperationContext(CacheAsMultiOperation<?> multiOperation, CacheOperationMetadata metadata, Object[] args, Object target) {
            super(metadata, args, target);
            this.multiOperation = multiOperation;
            this.superArgs = super.getArgs();
            this.cacheAsMultiArg = (Collection<?>) args[multiOperation.getCacheAsMultiParameterPosition()];
            this.isConditionPassingCache = CollectionUtils.newHashMap(cacheAsMultiArg.size());
            this.keyCache = CollectionUtils.newHashMap(cacheAsMultiArg.size());
        }

        @Override
        protected Collection<String> getCacheNames() {
            return super.getCacheNames();
        }

        @Override
        protected Collection<? extends EnhancedCache> getCaches() {
            return (Collection<? extends EnhancedCache>) super.getCaches();
        }

        protected boolean isConditionPassing(Object cacheAsMultiArgItem, @Nullable Object result) {
            return isConditionPassingCache.computeIfAbsent(cacheAsMultiArgItem, (argItem) -> {
                superArgs[multiOperation.getCacheAsMultiParameterPosition()] = cacheAsMultiArgItem;
                boolean passing = super.isConditionPassing(result);
                try {
                    CONTEXT_CONDITION_PASSING_FIELD.set(this, null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return passing;
            });
        }

        protected boolean canPutToCache(Object cacheAsMultiArgItem, @Nullable Object result) {
            superArgs[multiOperation.getCacheAsMultiParameterPosition()] = cacheAsMultiArgItem;

            return super.canPutToCache(result);
        }

        private Pair<Collection<?>, Collection<?>> splitIsConditionPassing(
                Collection<?> subCacheAsMultiArg, @Nullable Map<?, ?> argValueMap) {

            if (this.isConditionAllPassing == null) {
                if (!StringUtils.hasText(getOperation().getCondition())) {
                    this.isConditionAllPassing = true;
                }
            }

            if (this.isConditionAllPassing) {
                return Pair.of(subCacheAsMultiArg, Collections.emptyList());
            }
            if (argValueMap == null) {
                argValueMap = Collections.emptyMap();
            }

            return splitPassing(subCacheAsMultiArg, argValueMap, this::isConditionPassing);
        }

        private Pair<Collection<?>, Collection<?>> splitCanPutToCache(
                Collection<?> subCacheAsMultiArg, Map<?, ?> argValueMap) {

            String unless = "";
            CacheOperation operation = getOperation();
            if (operation instanceof CacheableOperation) {
                unless = ((CacheableOperation) operation).getUnless();
            } else if (operation instanceof CachePutOperation) {
                unless = ((CachePutOperation) operation).getUnless();
            }

            if (!StringUtils.hasText(unless)) {
                return Pair.of(subCacheAsMultiArg, Collections.emptyList());
            }
            return splitPassing(subCacheAsMultiArg, argValueMap, this::canPutToCache);
        }

        private Pair<Collection<?>, Collection<?>> splitPassing(
                Collection<?> subCacheAsMultiArg, Map<?, ?> argValueMap, ToBooleanBiFunction<Object, Object> function) {

            List<Object> allow = new ArrayList<>(subCacheAsMultiArg.size());
            List<Object> deny = new ArrayList<>(subCacheAsMultiArg.size());
            for (Object argItem : subCacheAsMultiArg) {
                boolean passing = function.applyAsBoolean(argItem, argValueMap.get(argItem));
                if (passing) {
                    allow.add(argItem);
                } else {
                    deny.add(argItem);
                }
            }

            return Pair.of(allow, deny);
        }

        protected Object generateKey(Object cacheAsMultiArgItem, @Nullable Object result) {
            return keyCache.computeIfAbsent(cacheAsMultiArgItem, (argItem) -> {
                superArgs[multiOperation.getCacheAsMultiParameterPosition()] = cacheAsMultiArgItem;
                Object key = super.generateKey(result);

                if (key == null) {
                    throw new IllegalArgumentException("Null key returned for cache operation (maybe you are " +
                            "using named params on classes without debug info?) " + super.getOperation());
                }

                return key;
            });
        }

        public Object[] getInvokeArg(Object subCacheAsMultiArg) {
            // 克隆一下，防止被生成key的时候改掉
            Object[] invokeArgs = superArgs.clone();
            invokeArgs[multiOperation.getCacheAsMultiParameterPosition()] = subCacheAsMultiArg;

            return invokeArgs;
        }
    }
}
