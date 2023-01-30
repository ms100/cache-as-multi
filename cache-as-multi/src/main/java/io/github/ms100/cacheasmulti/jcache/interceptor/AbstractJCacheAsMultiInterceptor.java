package io.github.ms100.cacheasmulti.jcache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.interceptor.AbstractCacheInvoker;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * 处理被{@link CacheAsMulti @CacheAsMulti}注解的方法的拦截器
 *
 * @author Zhumengshuai
 */
@Slf4j
abstract class AbstractJCacheAsMultiInterceptor<O extends AbstractJCacheAsMultiOperation<A>, A extends Annotation> extends AbstractCacheInvoker {

    public AbstractJCacheAsMultiInterceptor(CacheErrorHandler errorHandler) {
        super(errorHandler);
    }

    /**
     * 执行
     *
     * @param context
     * @param invoker
     * @return
     */
    @Nullable
    public abstract Object invoke(CacheAsMultiOperationContext<O, A> context, CacheOperationInvoker invoker);

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

    protected EnhancedCache resolveCache(CacheAsMultiOperationContext<O, A> context) {
        JCacheOperation<?> operation = context.getOperation();
        Collection<? extends Cache> caches = operation.getCacheResolver().resolveCaches(context);
        Cache cache = extractFrom(caches);
        if (cache == null) {
            throw new IllegalStateException("Cache could not have been resolved for " + operation);
        }
        return (EnhancedCache) cache;
    }

    @Nullable
    protected static Cache extractFrom(Collection<? extends Cache> caches) {
        if (CollectionUtils.isEmpty(caches)) {
            return null;
        } else if (caches.size() == 1) {
            return caches.iterator().next();
        } else {
            throw new IllegalStateException("Unsupported cache resolution result " + caches + ": JSR-107 only supports a single cache.");
        }
    }

}



