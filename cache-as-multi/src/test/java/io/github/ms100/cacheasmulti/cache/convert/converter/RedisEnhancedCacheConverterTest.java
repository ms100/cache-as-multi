package io.github.ms100.cacheasmulti.cache.convert.converter;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisEnhancedCacheConverterTest {

    @Test
    void executePropagatesWriterRuntimeFailureWithoutReflectionWrapper() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        IllegalStateException connectionFailure = new IllegalStateException("redis unavailable");
        when(connectionFactory.getConnection()).thenThrow(connectionFailure);
        RedisCacheWriter writer = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisEnhancedCacheConverter.RedisEnhancedCache cache =
                new RedisEnhancedCacheConverter.RedisEnhancedCache(
                        "test", writer, RedisCacheConfiguration.defaultCacheConfig());

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> cache.multiEvict(Collections.singleton("key")));

        assertSame(connectionFailure, thrown);
    }

    @Test
    void missingExecuteMethodIdentifiesTargetTypeAndMember() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> RedisEnhancedCacheConverter.getRequiredDeclaredMethod(String.class, "missingExecute"));

        assertTrue(thrown.getMessage().contains(String.class.getName()));
        assertTrue(thrown.getMessage().contains("missingExecute"));
    }
}
