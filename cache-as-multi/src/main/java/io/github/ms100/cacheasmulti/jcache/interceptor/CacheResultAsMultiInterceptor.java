package io.github.ms100.cacheasmulti.jcache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.interceptor.CacheAsMultiOperationInvoker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import javax.cache.annotation.CacheResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zhumengshuai
 */
@Slf4j
class CacheResultAsMultiInterceptor extends AbstractJCacheAsMultiInterceptor<CacheResultAsMultiOperation, CacheResult> {

    public CacheResultAsMultiInterceptor(CacheErrorHandler errorHandler) {
        super(errorHandler);
    }

    @Override
    @SneakyThrows
    @Nullable
    public Object invoke(CacheAsMultiOperationContext<CacheResultAsMultiOperation, CacheResult> context, CacheOperationInvoker invoker) {

        Collection<?> cacheAsMultiArg = (Collection<?>) context.getCacheAsMultiArg();
        // 如果@CacheAsMulti注解的参数值为null或者空集合，则调用原方法返回
        if (CollectionUtils.isEmpty(cacheAsMultiArg)) {
            return invoker.invoke();
        }

        // 参数与数据的映射
        Map<Object, Object> argValueMap = CollectionUtils.newHashMap(cacheAsMultiArg.size());

        Collection<?> missCacheAsMultiArg = findInCache(context, cacheAsMultiArg, argValueMap);

        if (log.isDebugEnabled()) {
            log.debug("Hit cache args " + argValueMap.keySet());
        }

        if (missCacheAsMultiArg.isEmpty()) {
            CacheResultAsMultiOperation multiOperation = context.getMultiOperation();
            return multiOperation.makeReturnObject(cacheAsMultiArg, argValueMap);
        }

        if (log.isDebugEnabled()) {
            log.debug("Miss cache args " + missCacheAsMultiArg);
        }
        // 查询 miss 的结果
        return invokeWithMissCacheAsMultiArg(context, invoker, missCacheAsMultiArg, argValueMap);
    }

    private Collection<?> findInCache(CacheAsMultiOperationContext<CacheResultAsMultiOperation, CacheResult> context,
                                      Collection<?> cacheAsMultiArg, Map<Object, Object> argValueMap) {

        Map<Object, Object> argKeyMap = CollectionUtils.newHashMap(cacheAsMultiArg.size());
        cacheAsMultiArg.forEach(argItem -> argKeyMap.put(argItem, context.generateKey(argItem)));
        if (log.isTraceEnabled()) {
            log.trace("Find keys " + argKeyMap.values() + " for operation " + context.getOperation());
        }

        CacheResultAsMultiOperation multiOperation = context.getMultiOperation();
        // 从缓存中取出的值
        final Map<Object, ValueWrapper> hitKeyValueWrapperMap;
        if (multiOperation.isAlwaysInvoked()) {
            hitKeyValueWrapperMap = null;
        } else {
            EnhancedCache cache = resolveCache(context);
            hitKeyValueWrapperMap = doMultiGet(cache, argKeyMap.values());
        }

        // 没有找到缓存的参数
        Collection<?> missCacheAsMultiArg;
        if (CollectionUtils.isEmpty(hitKeyValueWrapperMap)) {
            missCacheAsMultiArg = cacheAsMultiArg;
        } else {
            Collection<Object> newMissCacheAsMultiArg = new ArrayList<>(cacheAsMultiArg.size());
            argKeyMap.forEach((argItem, key) -> {
                ValueWrapper valueWrapper = hitKeyValueWrapperMap.get(key);
                if (valueWrapper != null) {
                    argValueMap.put(argItem, valueWrapper.get());
                } else {
                    newMissCacheAsMultiArg.add(argItem);
                }
            });
            missCacheAsMultiArg = multiOperation.newCacheAsMultiArg(newMissCacheAsMultiArg);
        }

        return missCacheAsMultiArg;
    }


    @Nullable
    private Object invokeWithMissCacheAsMultiArg(CacheAsMultiOperationContext<CacheResultAsMultiOperation, CacheResult> context,
                                                 CacheOperationInvoker invoker, Collection<?> missCacheAsMultiArg,
                                                 Map<Object, Object> argValueMap) {

        checkForCachedException(context, missCacheAsMultiArg);

        Object invokeValues = invokeOperation(context, invoker, missCacheAsMultiArg);
        CacheResultAsMultiOperation multiOperation = context.getMultiOperation();
        Map<?, ?> missArgValueMap = multiOperation.makeCacheMap(missCacheAsMultiArg, invokeValues);

        if (!CollectionUtils.isEmpty(missArgValueMap)) {
            // 需要缓存的数据
            Map<Object, Object> missKeyValueMap = CollectionUtils.newHashMap(missCacheAsMultiArg.size());
            missArgValueMap.forEach((argItem, value) -> missKeyValueMap.put(context.generateKey(argItem), value));
            EnhancedCache cache = resolveCache(context);
            // 缓存数据
            doMultiPut(cache, missKeyValueMap);

            // 如果缓存都未命中，直接返回执行结果
            if (argValueMap.size() == 0) {
                return invokeValues;
            }

            argValueMap.putAll(missArgValueMap);
        }

        Collection<?> cacheAsMultiArg = (Collection<?>) context.getCacheAsMultiArg();
        assert cacheAsMultiArg != null;
        return multiOperation.makeReturnObject(cacheAsMultiArg, argValueMap);
    }


    protected void checkForCachedException(CacheAsMultiOperationContext<CacheResultAsMultiOperation, ?> context,
                                           Collection<?> missCacheAsMultiArg) {

        EnhancedCache exceptionCache = resolveExceptionCache(context);

        if (exceptionCache != null) {
            Collection<Object> missKeys = missCacheAsMultiArg.stream().map(context::generateKey).collect(Collectors.toList());

            Map<Object, ValueWrapper> keyThrowableWrapperMap = this.doMultiGet(exceptionCache, missKeys);
            if (keyThrowableWrapperMap != null && keyThrowableWrapperMap.size() != 0) {
                Object ex = keyThrowableWrapperMap.values().stream().filter(Objects::nonNull).findFirst().orElse(() -> null).get();
                if (ex instanceof Throwable) {
                    throw new RuntimeException((Throwable) ex);
                }
            }
        }
    }

    @Nullable
    private EnhancedCache resolveExceptionCache(CacheAsMultiOperationContext<CacheResultAsMultiOperation, ?> context) {
        CacheResultAsMultiOperation operation = context.getMultiOperation();
        CacheResolver exceptionCacheResolver = operation.getExceptionCacheResolver();
        if (exceptionCacheResolver != null) {
            return (EnhancedCache) extractFrom(exceptionCacheResolver.resolveCaches(context));
        }
        return null;
    }

    @Nullable
    private Object invokeOperation(CacheAsMultiOperationContext<?, ?> context,
                                   CacheOperationInvoker invoker, Object missCacheAsMultiArg) {

        Object[] invokeArg = context.getInvokeArg(missCacheAsMultiArg);

        return ((CacheAsMultiOperationInvoker) invoker).invoke(invokeArg);
    }
}

