package io.github.ms100.cacheasmulti.jcache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.util.CollectionUtils;

import javax.cache.annotation.CachePut;
import java.util.Map;

/**
 * @author zhumengshuai
 */
@Slf4j
class CachePutAsMultiInterceptor extends AbstractJCacheAsMultiInterceptor<CachePutAsMultiOperation, CachePut> {

    public CachePutAsMultiInterceptor(CacheErrorHandler errorHandler) {
        super(errorHandler);
    }

    @Override
    @SneakyThrows
    public Object invoke(CacheAsMultiOperationContext<CachePutAsMultiOperation, CachePut> context, CacheOperationInvoker invoker) {
        CachePutAsMultiOperation multiOperation = context.getMultiOperation();
        boolean earlyPut = multiOperation.isEarlyPut();

        if (earlyPut) {
            cacheValues(context);
        }

        try {
            Object result = invoker.invoke();
            if (!earlyPut) {
                cacheValues(context);
            }
            return result;
        } catch (CacheOperationInvoker.ThrowableWrapper ex) {
            Throwable original = ex.getOriginal();
            if (!earlyPut && multiOperation.getExceptionTypeFilter().match(original.getClass())) {
                cacheValues(context);
            }
            throw ex;
        }
    }

    protected void cacheValues(CacheAsMultiOperationContext<CachePutAsMultiOperation, CachePut> context) {
        // 如果@CacheAsMulti注解的参数值为null或者空集合，则调用原方法返回
        Map<?, ?> cacheAsMultiArg = (Map<?, ?>) context.getCacheAsMultiArg();
        if (CollectionUtils.isEmpty(cacheAsMultiArg)) {
            return;
        }

        Map<Object, Object> keyValueMap = CollectionUtils.newHashMap(cacheAsMultiArg.size());
        cacheAsMultiArg.forEach((argItem, value) -> keyValueMap.put(context.generateKey(argItem), value));

        EnhancedCache cache = resolveCache(context);
        if (log.isTraceEnabled()) {
            log.trace("Store key-value map " + keyValueMap + " for operation " + context.getOperation());
        }
        doMultiPut(cache, keyValueMap);
    }

}

