package io.github.ms100.cacheasmulti.jcache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.util.CollectionUtils;

import javax.cache.annotation.CacheRemove;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author zhumengshuai
 */
@Slf4j
class CacheRemoveAsMultiInterceptor extends AbstractJCacheAsMultiInterceptor<CacheRemoveAsMultiOperation, CacheRemove> {

    public CacheRemoveAsMultiInterceptor(CacheErrorHandler errorHandler) {
        super(errorHandler);
    }

    @Override
    @SneakyThrows
    public Object invoke(CacheAsMultiOperationContext<CacheRemoveAsMultiOperation, CacheRemove> context, CacheOperationInvoker invoker) {
        CacheRemoveAsMultiOperation multiOperation = context.getMultiOperation();
        boolean earlyRemove = multiOperation.isEarlyRemove();

        if (earlyRemove) {
            removeValues(context);
        }

        try {
            Object result = invoker.invoke();
            if (!earlyRemove) {
                removeValues(context);
            }
            return result;
        } catch (CacheOperationInvoker.ThrowableWrapper wrapperException) {
            Throwable ex = wrapperException.getOriginal();
            if (!earlyRemove && multiOperation.getExceptionTypeFilter().match(ex.getClass())) {
                removeValues(context);
            }
            throw wrapperException;
        }
    }

    private void removeValues(CacheAsMultiOperationContext<CacheRemoveAsMultiOperation, CacheRemove> context) {
        // 如果@CacheAsMulti注解的参数值为null或者空集合，则调用原方法返回
        Collection<?> cacheAsMultiArg = (Collection<?>) context.getCacheAsMultiArg();
        if (CollectionUtils.isEmpty(cacheAsMultiArg)) {
            return;
        }

        Collection<Object> keys = new ArrayList<>(cacheAsMultiArg.size());
        cacheAsMultiArg.forEach(argItem -> keys.add(context.generateKey(argItem)));

        EnhancedCache cache = resolveCache(context);
        if (log.isTraceEnabled()) {
            log.trace("Evict keys " + keys + " for operation " + context.getOperation());
        }
        doMultiEvict(cache, keys);
    }
}

