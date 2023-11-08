package io.github.ms100.cacheasmulti.support.redis;

import lombok.Data;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author zhumengshuai
 */
@Data
@Order(0)
public class RedisCacheSerializeCustomizer implements RedisCacheManagerBuilderCustomizer {

    @Override
    public void customize(RedisCacheManager.RedisCacheManagerBuilder builder) {
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheCustomizerUtils.getDefaultCacheConfigurationFor(builder);

        defaultCacheConfiguration = defaultCacheConfiguration.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        builder.cacheDefaults(defaultCacheConfiguration);
    }

}
