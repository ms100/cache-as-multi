package io.github.ms100.cacheasmulti.support.redis;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author zhumengshuai
 */
public abstract class RedisCacheCustomizerUtils {

    private static final Field DEFAULT_CACHE_CONFIGURATION_FIELD;

    static {
        DEFAULT_CACHE_CONFIGURATION_FIELD = ReflectionUtils.findField(RedisCacheManager.RedisCacheManagerBuilder.class,
                "defaultCacheConfiguration", RedisCacheConfiguration.class);
        assert DEFAULT_CACHE_CONFIGURATION_FIELD != null;
        ReflectionUtils.makeAccessible(DEFAULT_CACHE_CONFIGURATION_FIELD);
    }

    public static RedisCacheConfiguration getDefaultCacheConfigurationFor(RedisCacheManager.RedisCacheManagerBuilder builder) {
        return (RedisCacheConfiguration) ReflectionUtils.getField(DEFAULT_CACHE_CONFIGURATION_FIELD, builder);
    }
}
