package io.github.ms100.cacheasmulti.cache.convert.converter;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import lombok.SneakyThrows;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhumengshuai
 */
public class RedisEnhancedCacheConverter implements EnhancedCacheConverter<RedisCache> {

    @Override
    public EnhancedCache convert(RedisCache source) {
        return new RedisEnhancedCache(source.getName(), source.getNativeCache(), source.getCacheConfiguration());
    }

    static class RedisEnhancedCache extends RedisCache implements EnhancedCache {

        private static final String CACHE_WRITER_CLASS = "org.springframework.data.redis.cache.DefaultRedisCacheWriter";
        private final Method executeMethod;

        @SneakyThrows
        protected RedisEnhancedCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
            super(name, cacheWriter, cacheConfig);
            Class<? extends RedisCacheWriter> cacheWriterClass = getNativeCache().getClass();
            if (!CACHE_WRITER_CLASS.equals(cacheWriterClass.getName())) {
                throw new IllegalStateException("cacheWriterClass must be " + CACHE_WRITER_CLASS);
            }

            executeMethod = cacheWriterClass.getDeclaredMethod("execute", String.class, Function.class);
            executeMethod.setAccessible(true);
        }

        @Override
        @SneakyThrows
        public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
            Object[] keyArr = keys.toArray(new Object[0]);
            byte[][] bytes = Arrays.stream(keyArr).map(this::createAndConvertCacheKey).toArray(byte[][]::new);
            List<byte[]> values = execute(connection -> connection.mGet(bytes));

            if (keyArr.length != values.size()) {
                throw new IllegalStateException();
            }

            HashMap<Object, ValueWrapper> map = CollectionUtils.newHashMap(keyArr.length);
            for (int i = 0, length = keyArr.length; i < length; i++) {
                byte[] value = values.get(i);
                ValueWrapper valueWrapper = toValueWrapper(value == null ? null : deserializeCacheValue(value));
                map.put(keyArr[i], valueWrapper);
            }

            return map;
        }

        @Override
        public void multiPut(Map<?, ?> map) {
            Stream<? extends Map.Entry<?, ?>> stream = map.entrySet().stream();

            if (!isAllowNullValues()) {
                stream = stream.filter(entry -> entry.getValue() != null);
            }
            Map<byte[], byte[]> collect = stream.collect(Collectors.toMap(
                    entry -> createAndConvertCacheKey(entry.getKey()), entry -> {
                        Object cacheValue = preProcessCacheValue(entry.getValue());
                        assert cacheValue != null;
                        return serializeCacheValue(cacheValue);
                    }));

            Duration ttl = getCacheConfiguration().getTtl();
            if (shouldExpireWithin(ttl)) {
                execute(connection -> {
                    connection.openPipeline();
                    boolean pipelinedClosed = false;
                    try {
                        collect.forEach((key, value) -> connection.set(key, value,
                                Expiration.from(ttl.toMillis(), TimeUnit.MILLISECONDS),
                                RedisStringCommands.SetOption.upsert()));
                        connection.closePipeline();
                        pipelinedClosed = true;
                    } finally {
                        if (!pipelinedClosed) {
                            connection.closePipeline();
                        }
                    }

                    return "OK";
                });
            } else {
                execute(connection -> {
                    connection.mSet(collect);
                    return "OK";
                });
            }
        }

        @Override
        public void multiEvict(Collection<?> keys) {
            byte[][] bytes = keys.stream().map(this::createAndConvertCacheKey).toArray(byte[][]::new);
            execute(connection -> connection.del(bytes));
        }

        private byte[] createAndConvertCacheKey(Object key) {
            return serializeCacheKey(createCacheKey(key));
        }

        private static boolean shouldExpireWithin(@Nullable Duration ttl) {
            return ttl != null && !ttl.isZero() && !ttl.isNegative();
        }

        @SneakyThrows
        private <T> T execute(Function<RedisConnection, T> callback) {
            return (T) executeMethod.invoke(getNativeCache(), getName(), callback);
        }
    }
}
