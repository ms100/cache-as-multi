package io.github.ms100.cacheasmulti.support;

import lombok.Setter;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author zhumengshuai
 */
@ConditionalOnClass(RedisCacheManager.class)
@ConditionalOnProperty(prefix = "spring.cache.redis.specific-time-to-live", name = "enabled", havingValue = "true")
@ConfigurationProperties(prefix = "spring.cache.redis.specific-time-to-live")
@Component
public class RedisCacheSpecificTimeToLiveCustomizer implements RedisCacheManagerBuilderCustomizer {

    @Setter
    Map<String, Duration> properties;

    @Override
    public void customize(RedisCacheManager.RedisCacheManagerBuilder builder) {
        builder.initialCacheNames(Collections.singleton("*"));
        RedisCacheConfiguration defaultCacheConfiguration = builder.getCacheConfigurationFor("*")
                .orElseThrow(NullPointerException::new);

        properties.forEach((cacheName, ttl) -> {
            RedisCacheConfiguration configuration = defaultCacheConfiguration.entryTtl(ttl);
            builder.withCacheConfiguration(cacheName, configuration);
        });
    }

}
