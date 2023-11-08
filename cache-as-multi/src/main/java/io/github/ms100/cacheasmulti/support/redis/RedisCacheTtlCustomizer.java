package io.github.ms100.cacheasmulti.support.redis;

import lombok.Data;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author zhumengshuai
 */
@Data
@Order
public class RedisCacheTtlCustomizer implements RedisCacheManagerBuilderCustomizer {
    /**
     * CacheName 对应的 TTL(PT 时间格式)
     */
    private Map<String, Duration> cacheNameTimeToLiveMap = Collections.emptyMap();

    @Override
    public void customize(RedisCacheManager.RedisCacheManagerBuilder builder) {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheCustomizerUtils.getDefaultCacheConfigurationFor(builder);

        cacheNameTimeToLiveMap.forEach((cacheName, ttl) -> {
            RedisCacheConfiguration configuration = defaultCacheConfiguration.entryTtl(ttl);
            builder.withCacheConfiguration(cacheName, configuration);
        });
    }

}
