package io.github.ms100.cacheasmulti.cache.convert.converter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import lombok.SneakyThrows;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhumengshuai
 */
public class CaffeineEnhancedCacheConverter implements EnhancedCacheConverter<CaffeineCache> {

    @SneakyThrows
    @Override
    public EnhancedCache convert(CaffeineCache source) {
        return new CaffeineEnhancedCache(source.getName(), source.getNativeCache(), source.isAllowNullValues());
    }

    public static class CaffeineEnhancedCache extends CaffeineCache implements EnhancedCache {

        public CaffeineEnhancedCache(String name, Cache<Object, Object> cache, boolean allowNullValues) {
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

            Map<Object, ValueWrapper> newMap = new HashMap<>(keys.size());
            map.forEach((key, value) -> newMap.put(key, toValueWrapper(value)));

            return newMap;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            Map<Object, Object> newMap = new HashMap<>(map.size());
            map.forEach((key, value) -> newMap.put(key, toStoreValue(value)));

            getNativeCache().putAll(newMap);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            getNativeCache().invalidateAll(keys);
        }

    }
}