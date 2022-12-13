package io.github.ms100.cacheasmulti.jcache.service;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import java.lang.annotation.Annotation;
import java.util.Arrays;

public class FooKeyGenerator implements CacheKeyGenerator {

    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        CacheInvocationParameter[] keyParameters = cacheKeyInvocationContext.getKeyParameters();
        Object[] objects = Arrays.stream(keyParameters).map(CacheInvocationParameter::getValue).toArray(Object[]::new);

        return new FooKey(cacheKeyInvocationContext.getTarget().getClass().getSimpleName(), objects);
    }
}

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
class FooKey implements GeneratedCacheKey {
    private final String name;
    private final Object[] fooId;
}