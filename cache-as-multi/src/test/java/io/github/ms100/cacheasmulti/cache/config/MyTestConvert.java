package io.github.ms100.cacheasmulti.cache.config;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.convert.converter.EnhancedCacheConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

@Component
public class MyTestConvert implements EnhancedCacheConverter<MyTestConvert.MyTestCache> {


    @Nullable
    @Override
    public EnhancedCache convert(MyTestCache source) {
        return new EnhancedMyTestCache(source.getName());
    }


    @RequiredArgsConstructor
    public static class MyTestCache implements Cache {

        private final String name;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return null;
        }

        @Nullable
        @Override
        public ValueWrapper get(Object key) {
            return null;
        }

        @Nullable
        @Override
        public <T> T get(Object key, @Nullable Class<T> type) {
            return null;
        }

        @Nullable
        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            return null;
        }

        @Override
        public void put(Object key, @Nullable Object value) {

        }

        @Override
        public void evict(Object key) {

        }

        @Override
        public void clear() {

        }
    }

    public static class EnhancedMyTestCache extends MyTestCache implements EnhancedCache {

        public EnhancedMyTestCache(String name) {
            super(name);
        }

        @Override
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            return null;
        }

        @Override
        public void multiPut(Map<?, ?> map) {

        }

        @Override
        public void multiEvict(Collection<?> keys) {

        }
    }
}
