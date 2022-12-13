package io.github.ms100.cacheasmulti.cache.convert.converter;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.serializer.support.SerializationDelegate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhumengshuai
 */
@ConditionalOnBean(EnhancedCacheConversionService.class)
@ConditionalOnClass(ConcurrentMapCache.class)
@Component
public class ConcurrentMapCacheToEnhancedCacheConverter implements EnhancedCacheConverter<ConcurrentMapCache> {

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

        return new EnhancedConcurrentMapCache(source.getName(), source.getNativeCache(),
                source.isAllowNullValues(), serialization);
    }

    public static class EnhancedConcurrentMapCache extends ConcurrentMapCache implements EnhancedCache {

        protected EnhancedConcurrentMapCache(String name, ConcurrentMap<Object, Object> store,
                                             boolean allowNullValues, @Nullable SerializationDelegate serialization) {
            super(name, store, allowNullValues, serialization);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Map<Object, ValueWrapper> map = CollectionUtils.newHashMap(keys.size());

            keys.forEach(key -> {
                ValueWrapper valueWrapper = this.get(key);
                map.put(key, valueWrapper);
            });

            return map;
        }

        @Override
        @SneakyThrows
        public void multiPut(Map<?, ?> map) {
            Map<Object, Object> newMap = CollectionUtils.newHashMap(map.size());
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