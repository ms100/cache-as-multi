package io.github.ms100.cacheasmulti.jcache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.cache.jcache.interceptor.SimpleExceptionCacheResolver;
import org.springframework.lang.Nullable;

import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheResult;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 被{@link CacheAsMulti @CacheAsMulti}注解的方法，在拦截器执行时，方法上的上下文状态
 * 对外表现使用{@link CacheResult}做泛型参数，已使得Spring的类可以正常处理它，例如{@link SimpleExceptionCacheResolver}。
 *
 * @author zhumengshuai
 */

@Getter
@Slf4j
class CacheAsMultiOperationContext<O extends AbstractJCacheAsMultiOperation<A>, A extends Annotation>
        implements CacheInvocationContext<A>, CacheOperationInvocationContext<JCacheOperation<A>> {

    private final O multiOperation;

    private final JCacheOperation<A> operation;

    private final Object target;

    private final Object[] args;

    @Nullable
    private final Object cacheAsMultiArg;

    private final CacheInvocationParameter[] allParameters;

    private final Map<Object, Object> keyCache;

    public CacheAsMultiOperationContext(O multiOperation, Object target, Object[] args) {
        this.multiOperation = multiOperation;
        this.operation = multiOperation.getOperation();
        this.target = target;
        /**
         * args数组是{@link MethodInvocation#getArguments()}的引用
         * 改变了它就改变了{@link MethodInvocation#proceed()}的参数
         * 因为计算key的时候需要改变它，为了防止影响所以这里克隆下
         */
        this.args = args.clone();
        this.cacheAsMultiArg = args[multiOperation.getCacheAsMultiParameterPosition()];
        this.allParameters = this.operation.getAllParameters(args);
        int size = 0;
        if (cacheAsMultiArg instanceof Collection) {
            size = ((Collection<?>) cacheAsMultiArg).size();
        } else if (cacheAsMultiArg instanceof Map) {
            size = ((Map<?, ?>) cacheAsMultiArg).size();
        }
        this.keyCache = new HashMap<>(size);
    }

    @Override
    public Method getMethod() {
        return operation.getMethod();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new IllegalArgumentException("Cannot unwrap to " + cls);
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return operation.getAnnotations();
    }

    @Override
    public A getCacheAnnotation() {
        return operation.getCacheAnnotation();
    }

    @Override
    public String getCacheName() {
        return operation.getCacheName();
    }

    public Object generateKey(Object cacheAsMultiArgItem) {
        return keyCache.computeIfAbsent(cacheAsMultiArgItem, (argItem) -> {
            args[multiOperation.getCacheAsMultiParameterPosition()] = argItem;
            return multiOperation.getKeyGenerator().generate(target, operation.getMethod(), args);
        });
    }

    public Object[] getInvokeArg(Object subCacheAsMultiArg) {
        // 克隆一下，防止被生成key的时候改掉
        Object[] invokeArgs = args.clone();
        invokeArgs[multiOperation.getCacheAsMultiParameterPosition()] = subCacheAsMultiArg;

        return invokeArgs;
    }

}