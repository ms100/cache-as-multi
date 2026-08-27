package io.github.ms100.cacheasmulti.support.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

class RedisCacheCustomizersTest {

    @Test
    void ttlCustomizerConfiguresNamedCacheWithoutConnectingToRedis() {
        RedisCacheManager.RedisCacheManagerBuilder builder = builder();
        RedisCacheTtlCustomizer customizer = new RedisCacheTtlCustomizer();
        customizer.setCacheNameTimeToLiveMap(Collections.singletonMap("short-lived", Duration.ofSeconds(30)));

        customizer.customize(builder);

        RedisCacheConfiguration configuration = builder
                .getCacheConfigurationFor("short-lived")
                .orElseThrow(AssertionError::new);
        assertEquals(Duration.ofSeconds(30), configuration.getTtl());
    }

    @Test
    void serializeCustomizerReplacesDefaultValueSerializationWithoutConnectingToRedis() {
        RedisCacheManager.RedisCacheManagerBuilder builder = builder();
        RedisCacheConfiguration original = RedisCacheCustomizerUtils.getDefaultCacheConfigurationFor(builder);

        new RedisCacheSerializeCustomizer().customize(builder);

        RedisCacheConfiguration customized = RedisCacheCustomizerUtils.getDefaultCacheConfigurationFor(builder);
        assertNotEquals(original.getValueSerializationPair(), customized.getValueSerializationPair());
    }

    private RedisCacheManager.RedisCacheManagerBuilder builder() {
        return RedisCacheManager.builder(mock(RedisCacheWriter.class))
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));
    }
}
