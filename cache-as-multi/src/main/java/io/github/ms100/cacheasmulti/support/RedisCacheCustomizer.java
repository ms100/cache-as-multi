package io.github.ms100.cacheasmulti.support;

import lombok.Data;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * @author zhumengshuai
 */
@Data
public class RedisCacheCustomizer implements RedisCacheManagerBuilderCustomizer {
    /**
     * 使用 RedisSerializer.json 序列化
     */
    private boolean serializeToJson = false;
    /**
     * CacheName 对应的 TTL(PT 时间格式)
     */
    private Map<String, Duration> cacheNameTimeToLiveMap = Collections.emptyMap();

    @Override
    public void customize(RedisCacheManager.RedisCacheManagerBuilder builder) {
        //用一个假的 cacheName 获取到默认的 RedisCacheConfiguration
        builder.initialCacheNames(Collections.singleton("*"));
        RedisCacheConfiguration defaultCacheConfiguration = builder.getCacheConfigurationFor("*")
                .orElseThrow(NullPointerException::new);

        if (serializeToJson) {
            defaultCacheConfiguration = defaultCacheConfiguration.serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
            builder.cacheDefaults(defaultCacheConfiguration);
        }
        RedisCacheConfiguration finalDefaultCacheConfiguration = defaultCacheConfiguration;
        cacheNameTimeToLiveMap.forEach((cacheName, ttl) -> {
            RedisCacheConfiguration configuration = finalDefaultCacheConfiguration.entryTtl(ttl);
            builder.withCacheConfiguration(cacheName, configuration);
        });
    }

}
