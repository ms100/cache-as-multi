package io.github.ms100.cacheasmulti.jcache.interceptor;


import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.util.ExceptionTypeFilter;

import javax.cache.annotation.CacheRemove;

/**
 * @author zhumengshuai
 */
@Slf4j
class CacheRemoveAsMultiOperation extends AbstractJCacheAsMultiOperation<CacheRemove> {

    @Getter
    private final ExceptionTypeFilter exceptionTypeFilter;

    @SneakyThrows
    public CacheRemoveAsMultiOperation(JCacheOperation<CacheRemove> operation,
                                       KeyGenerator keyGenerator, CacheAsMultiParameterDetail parameterDetail) {

        super(operation, keyGenerator, parameterDetail);
        CacheRemove ann = operation.getCacheAnnotation();
        exceptionTypeFilter = initializeExceptionTypeFilter(ann.evictFor(), ann.noEvictFor());
    }

    public boolean isEarlyRemove() {
        return !operation.getCacheAnnotation().afterInvocation();
    }
}
