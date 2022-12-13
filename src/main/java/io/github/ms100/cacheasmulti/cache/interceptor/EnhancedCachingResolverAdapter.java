package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


/**
 * 包装一下Spring的{@link AbstractCacheResolver}
 * 将原本返回的Spring的{@link Cache}转成{@link EnhancedCache}
 *
 * @author zhumengshuai
 */
@RequiredArgsConstructor
public class EnhancedCachingResolverAdapter implements EnhancedCacheResolver {

    private final ConcurrentMap<Cache, EnhancedCache> enhancedCacheCache = new ConcurrentHashMap<>(1024);

    private final CacheResolver cacheResolver;

    private final EnhancedCacheConversionService conversionService;

    @Override
    public Collection<? extends EnhancedCache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<? extends Cache> caches = cacheResolver.resolveCaches(context);

        if (caches.isEmpty()) {
            return Collections.emptyList();
        }
        return caches.stream().map(this::convert).collect(Collectors.toList());
    }

    private EnhancedCache convert(Cache cache) {
        return enhancedCacheCache.computeIfAbsent(cache, conversionService::convert);
    }

}
