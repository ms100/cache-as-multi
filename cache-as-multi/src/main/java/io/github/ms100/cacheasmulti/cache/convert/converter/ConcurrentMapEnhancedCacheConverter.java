package io.github.ms100.cacheasmulti.cache.convert.converter;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import lombok.SneakyThrows;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.serializer.support.SerializationDelegate;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhumengshuai
 */
public class ConcurrentMapEnhancedCacheConverter implements EnhancedCacheConverter<ConcurrentMapCache> {

    private static final Field SERIALIZATION_FIELD;

    static {
        try {
            SERIALIZATION_FIELD = ConcurrentMapCache.class.getDeclaredField("serialization");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        SERIALIZATION_FIELD.setAccessible(true);
    }

    @SneakyThrows
    @Override
    public EnhancedCache convert(ConcurrentMapCache source) {
        SerializationDelegate serialization;
        if (source.isStoreByValue()) {
            serialization = (SerializationDelegate) SERIALIZATION_FIELD.get(source);
        } else {
            serialization = null;
        }

        return new ConcurrentMapEnhancedCache(source.getName(), source.getNativeCache(),
                source.isAllowNullValues(), serialization);
    }

    public static class ConcurrentMapEnhancedCache extends ConcurrentMapCache implements EnhancedCache {

        protected ConcurrentMapEnhancedCache(String name, ConcurrentMap<Object, Object> store,
                                             boolean allowNullValues, @Nullable SerializationDelegate serialization) {
            super(name, store, allowNullValues, serialization);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Map<Object, ValueWrapper> map = new HashMap<>(keys.size());

            keys.forEach(key -> {
                ValueWrapper valueWrapper = this.get(key);
                map.put(key, valueWrapper);
            });

            return map;
        }

        @Override
        @SneakyThrows
        public void multiPut(Map<?, ?> map) {
            Map<Object, Object> newMap = new HashMap<>(map.size());
            map.forEach((k, v) -> newMap.put(k, toStoreValue(v)));

            ConcurrentMap<Object, Object> store = getNativeCache();
            store.putAll(map);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            keys.forEach(this::evict);
        }

    }
}