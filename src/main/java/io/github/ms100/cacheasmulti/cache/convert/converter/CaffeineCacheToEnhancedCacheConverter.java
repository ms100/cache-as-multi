package io.github.ms100.cacheasmulti.cache.convert.converter;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author zhumengshuai
 */
@ConditionalOnBean(EnhancedCacheConversionService.class)
@ConditionalOnClass(Cache.class)
@Component
public class CaffeineCacheToEnhancedCacheConverter implements EnhancedCacheConverter<CaffeineCache> {

    @SneakyThrows
    @Override
    public EnhancedCache convert(CaffeineCache source) {
        return new EnhancedCaffeineCache(source.getName(), source.getNativeCache(), source.isAllowNullValues());
    }

    public static class EnhancedCaffeineCache extends CaffeineCache implements EnhancedCache {

        public EnhancedCaffeineCache(String name, Cache<Object, Object> cache, boolean allowNullValues) {
            super(name, cache, allowNullValues);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Cache<Object, Object> cache = getNativeCache();

            Map<@NonNull Object, @NonNull Object> map;
            if (cache instanceof LoadingCache) {
                map = ((LoadingCache<Object, Object>) cache).getAll(keys);
            } else {
                map = cache.getAllPresent(keys);
            }

            Map<Object, ValueWrapper> newMap = CollectionUtils.newHashMap(keys.size());
            map.forEach((key, value) -> newMap.put(key, toValueWrapper(value)));

            return newMap;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            Map<Object, Object> newMap = CollectionUtils.newHashMap(map.size());
            map.forEach((key, value) -> newMap.put(key, toStoreValue(value)));

            getNativeCache().putAll(newMap);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            getNativeCache().invalidateAll(keys);
        }

    }
}