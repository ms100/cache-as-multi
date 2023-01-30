package io.github.ms100.cacheasmulti.cache.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.lang.Nullable;

/**
 * @author zhumengshuai
 */
@RequiredArgsConstructor
public class CacheAsMultiOperationInvoker implements CacheOperationInvoker {

    private final ProxyMethodInvocation invocation;

    @Nullable
    @Override
    public Object invoke() throws ThrowableWrapper {
        try {
            return invocation.proceed();
        } catch (Throwable ex) {
            throw new CacheOperationInvoker.ThrowableWrapper(ex);
        }
    }


    @Nullable
    public Object invoke(Object[] args) throws ThrowableWrapper {
        try {
            return invocation.invocableClone(args).proceed();
        } catch (Throwable ex) {
            throw new CacheOperationInvoker.ThrowableWrapper(ex);
        }
    }
}
