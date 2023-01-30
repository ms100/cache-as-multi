package io.github.ms100.cacheasmulti.support;

import lombok.Setter;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author zhumengshuai
 */
public class RedisCacheNameTimeToLiveCustomizer implements RedisCacheManagerBuilderCustomizer {

    /**
     * CacheName 对应的 TTL(PT 时间格式)
     */
    @Setter
    Map<String, Duration> maps = Collections.emptyMap();

    @Override
    public void customize(RedisCacheManager.RedisCacheManagerBuilder builder) {
        builder.initialCacheNames(Collections.singleton("*"));
        RedisCacheConfiguration defaultCacheConfiguration = builder.getCacheConfigurationFor("*")
                .orElseThrow(NullPointerException::new);

        maps.forEach((cacheName, ttl) -> {
            RedisCacheConfiguration configuration = defaultCacheConfiguration.entryTtl(ttl);
            builder.withCacheConfiguration(cacheName, configuration);
        });
    }

}
