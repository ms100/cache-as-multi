package io.github.ms100.cacheasmulti.cache.convert.converter;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import lombok.SneakyThrows;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author zhumengshuai
 */
@ConditionalOnBean(EnhancedCacheConversionService.class)
@ConditionalOnClass(Ehcache.class)
@Component
public class EhcacheCacheToEnhancedCacheConverter implements EnhancedCacheConverter<EhCacheCache> {

    @SneakyThrows
    @Override
    public EnhancedCache convert(EhCacheCache source) {
        return new EnhancedEhcacheCache(source.getNativeCache());
    }

    public static class EnhancedEhcacheCache extends EhCacheCache implements EnhancedCache {

        public EnhancedEhcacheCache(Ehcache ehcache) {
            super(ehcache);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Map<Object, Element> map = getNativeCache().getAll(keys);

            Map<Object, ValueWrapper> newMap = CollectionUtils.newHashMap(keys.size());
            map.forEach((key, value) -> newMap.put(key, toValueWrapper(value)));

            return newMap;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            Collection<Element> elements = new ArrayList<>(map.size());
            map.forEach((key, value) -> elements.add(new Element(key, value)));

            getNativeCache().putAll(elements);
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            getNativeCache().removeAll(keys);
        }

        @Nullable
        private ValueWrapper toValueWrapper(@Nullable Element element) {
            return (element != null ? new SimpleValueWrapper(element.getObjectValue()) : null);
        }
    }
}