package io.github.ms100.cacheasmulti.cache.convert;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.config.MyTestConvert;
import io.github.ms100.cacheasmulti.cache.config.MyTestConvert.MyTestCache;
import io.github.ms100.cacheasmulti.cache.convert.converter.ConcurrentMapEnhancedCacheConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableCaching
@Slf4j
class EnhancedCacheConversionServiceTest {

    @Autowired
    private EnhancedCacheConversionService conversionService;

    @Autowired(required = false)
    private RedisCacheManager redisCacheManager;

    @Test
    void convert() {
        ConcurrentMapCache cache = new ConcurrentMapCache("test");
        EnhancedCache convertConcurrentMap = conversionService.convert(cache);
        assertEquals("test", convertConcurrentMap.getName());
        assertTrue(convertConcurrentMap instanceof ConcurrentMapEnhancedCacheConverter.ConcurrentMapEnhancedCache);

        if (redisCacheManager != null) {
            log.info("redisCacheManager 存在，测试 redis ...");
            Cache redisCache = redisCacheManager.getCache("test1");
            assert redisCache != null;
            EnhancedCache convertRedis = conversionService.convert(redisCache);
            assertEquals("test1", convertRedis.getName());
            //assertTrue(convertRedis instanceof RedisEnhancedCacheConverter.EnhancedRedisCache);
        }

        MyTestCache myTestCache = new MyTestCache("mytest");
        EnhancedCache convertMyTest = conversionService.convert(myTestCache);
        assertEquals("mytest", convertMyTest.getName());
        assertTrue(convertMyTest instanceof MyTestConvert.EnhancedMyTestCache);

        MyTest2Cache myTest2Cache = new MyTest2Cache("mytest2");
        EnhancedCache convertMyTest2 = conversionService.convert(myTest2Cache);
        assertEquals("mytest2", convertMyTest2.getName());
        assertTrue(convertMyTest2 instanceof EnhancedCacheConversionService.EnhancedCacheAdapter);
    }

    @RequiredArgsConstructor
    static class MyTest2Cache implements Cache {
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
}