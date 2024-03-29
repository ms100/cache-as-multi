package io.github.ms100.cacheasmulti.jcache.interceptor;


import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.util.ExceptionTypeFilter;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheValue;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhumengshuai
 */
@Slf4j
class CachePutAsMultiOperation extends AbstractJCacheAsMultiOperation<CachePut> {

    @Getter
    private final ExceptionTypeFilter exceptionTypeFilter;

    @SneakyThrows
    public CachePutAsMultiOperation(JCacheOperation<CachePut> operation,
                                    KeyGenerator keyGenerator, CacheAsMultiParameterDetail parameterDetail) {

        super(operation, keyGenerator, parameterDetail);
        CachePut ann = operation.getCacheAnnotation();
        exceptionTypeFilter = initializeExceptionTypeFilter(ann.cacheFor(), ann.noCacheFor());
        // @CacheValue注解参数默认不会用来做key，所以需要将它放进去
        initializeCachePutOperationKeyParameterDetails(operation, this.parameterDetail);
    }

    @Override
    protected void validateParameterDetail(Method method, CacheAsMultiParameterDetail parameterDetail) {
        // 如果方法被@CachePut注解，那@CacheAsMulti注解必须在@CacheValue注解的参数上
        if (!parameterDetail.isAnnotationPresent(CacheValue.class)) {
            throw new IllegalStateException("The @CacheAsMulti parameter should be same as @CacheValue on " + method);
        }
        Class<?> rawType = parameterDetail.getRawType();
        // 参数必须是Map类型
        if (!Map.class.isAssignableFrom(rawType)) {
            throw new IllegalStateException("The @CacheAsMulti parameter should be a map on " + method);
        }
    }

    public boolean isEarlyPut() {
        return !operation.getCacheAnnotation().afterInvocation();
    }

    private static void initializeCachePutOperationKeyParameterDetails(
            JCacheOperation<?> operation, CacheAsMultiParameterDetail parameterDetail) {

        // 如果@CacheAsMulti参数上有@CacheKey，直接返回
        if (parameterDetail.isAnnotationPresent(CacheKey.class)) {
            return;
        }

        // 此时所有参数均为@CacheKey
        List<Object> keyParameterList = (List<Object>) getCacheOperationField(operation, "keyParameterDetails");
        Objects.requireNonNull(keyParameterList);
        List<Object> allParameterList = (List<Object>) getCacheOperationField(operation, "allParameterDetails");
        Objects.requireNonNull(allParameterList);
        keyParameterList.clear();
        keyParameterList.addAll(allParameterList);
    }
}
