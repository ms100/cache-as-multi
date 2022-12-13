package io.github.ms100.cacheasmulti.cache.convert;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.convert.converter.EnhancedCacheConverter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.cache.Cache;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 缓存转换服务，将Spring的{@link Cache}转成{@link EnhancedCache}
 *
 * @author zhumengshuai
 */
public class EnhancedCacheConversionService extends GenericConversionService implements EnhancedCacheConverter<Cache> {

    public EnhancedCacheConversionService(Collection<EnhancedCacheConverter<?>> converters) {
        converters.forEach(this::addConverter);
    }

    @Override
    @NonNull
    public EnhancedCache convert(Cache source) {
        if (super.canConvert(source.getClass(), EnhancedCache.class)) {
            return Objects.requireNonNull(super.convert(source, EnhancedCache.class));
        } else {
            return new EnhancedCacheAdapter(source);
        }
    }

    /**
     * 对Spring的{@link Cache}进行包装，变为{@link EnhancedCache}
     * 只是作为兜底，尽量不要直接使用
     *
     * @author zhumengshuai
     */
    @RequiredArgsConstructor
    static class EnhancedCacheAdapter implements EnhancedCache {
        @Delegate
        private final Cache cache;

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            HashMap<Object, ValueWrapper> map = CollectionUtils.newHashMap(keys.size());
            keys.forEach(k -> {
                ValueWrapper valueWrapper = cache.get(k);
                map.put(k, valueWrapper);
            });

            return map;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            map.forEach(cache::put);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            keys.forEach(cache::evict);
        }
    }

}
