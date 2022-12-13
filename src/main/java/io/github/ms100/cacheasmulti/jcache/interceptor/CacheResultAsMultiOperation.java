package io.github.ms100.cacheasmulti.jcache.interceptor;


import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.lang.Nullable;

import javax.cache.annotation.CacheResult;

/**
 * @author zhumengshuai
 */
@Slf4j
class CacheResultAsMultiOperation extends AbstractJCacheAsMultiOperation<CacheResult> {

    @Nullable
    @Getter
    private final CacheResolver exceptionCacheResolver;

    @SneakyThrows
    public CacheResultAsMultiOperation(JCacheOperation<CacheResult> operation, KeyGenerator keyGenerator,
                                       CacheAsMultiParameterDetail parameterDetail, @Nullable CacheResolver exceptionCacheResolver) {

        super(operation, keyGenerator, parameterDetail);
        this.exceptionCacheResolver = exceptionCacheResolver;

        if (returnTypeMaker == null) {
            throw new IllegalStateException("The returnType must not be null on " + operation.getMethod().getName());
        }
    }

    public boolean isAlwaysInvoked() {
        return operation.getCacheAnnotation().skipGet();
    }

}
