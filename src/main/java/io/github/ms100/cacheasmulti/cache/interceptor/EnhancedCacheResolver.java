package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;

public interface EnhancedCacheResolver extends CacheResolver {

    /**
     * 返回用于指定调用的缓存
     *
     * @param context the context of the particular invocation
     * @return EnhancedCache
     */
    @Override
    Collection<? extends EnhancedCache> resolveCaches(CacheOperationInvocationContext<?> context);

}
