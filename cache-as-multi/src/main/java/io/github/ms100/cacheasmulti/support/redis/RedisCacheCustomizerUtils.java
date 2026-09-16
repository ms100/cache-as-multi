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
        DEFAULT_CACHE_CONFIGURATION_FIELD = getRequiredField(RedisCacheManager.RedisCacheManagerBuilder.class,
                "defaultCacheConfiguration", RedisCacheConfiguration.class);
        ReflectionUtils.makeAccessible(DEFAULT_CACHE_CONFIGURATION_FIELD);
    }

    static Field getRequiredField(Class<?> targetType, String fieldName, Class<?> fieldType) {
        Field field = ReflectionUtils.findField(targetType, fieldName, fieldType);
        if (field == null) {
            throw new IllegalStateException("Required field '" + fieldName + "' not found on type "
                    + targetType.getName());
        }
        return field;
    }

    public static RedisCacheConfiguration getDefaultCacheConfigurationFor(RedisCacheManager.RedisCacheManagerBuilder builder) {
        return (RedisCacheConfiguration) ReflectionUtils.getField(DEFAULT_CACHE_CONFIGURATION_FIELD, builder);
    }
}
