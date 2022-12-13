package io.github.ms100.cacheasmulti.cache.config;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
enum CacheMatchingBeanNames {
    CACHING(
            "cacheInterceptor",
            "cacheOperationSource",
            CacheMatchingBeanNames.CACHING_FACTORY_BEAN_NAME
    ),
    JCACHE(
            "jCacheInterceptor",
            "jCacheOperationSource",
            CacheMatchingBeanNames.JCACHE_FACTORY_BEAN_NAME
    );

    static final String CACHING_FACTORY_BEAN_NAME = "enhancedCachingConfiguration";

    static final String JCACHE_FACTORY_BEAN_NAME = "enhancedJCacheConfiguration";

    public final String interceptorBeanName;
    public final String operationSourceBeanName;
    public final String factoryBeanName;
}
