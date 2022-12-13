package io.github.ms100.cacheasmulti.jcache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import io.github.ms100.cacheasmulti.cache.interceptor.AbstractCacheAsMultiOperation;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.interceptor.JCacheOperation;
import org.springframework.util.ExceptionTypeFilter;
import org.springframework.util.ReflectionUtils;

import javax.cache.annotation.CacheKey;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

/**
 * 被{@link  CacheAsMulti @CacheAsMulti}注解的方法进行解析的结果，
 * 缓存在{@link EnhancedJCacheOperationSource}中
 *
 * @author Zhumengshuai
 */
@Slf4j
abstract class AbstractJCacheAsMultiOperation<A extends Annotation> extends AbstractCacheAsMultiOperation {

    @Getter
    protected final JCacheOperation<A> operation;

    @Getter
    protected final KeyGenerator keyGenerator;

    public AbstractJCacheAsMultiOperation(JCacheOperation<A> operation,
                                          KeyGenerator keyGenerator, CacheAsMultiParameterDetail parameterDetail) {
        super(operation.getMethod(), parameterDetail);
        this.operation = operation;
        this.keyGenerator = keyGenerator;
        validateParameterDetail(operation, parameterDetail);
    }

    protected static ExceptionTypeFilter initializeExceptionTypeFilter(
            Class<? extends Throwable>[] includes, Class<? extends Throwable>[] excludes) {

        return new ExceptionTypeFilter(Arrays.asList(includes), Arrays.asList(excludes), true);
    }

    protected static void validateParameterDetail(
            JCacheOperation<?> operation, CacheAsMultiParameterDetail parameterDetail) {

        Method method = operation.getMethod();
        // 如果@CacheAsMulti注解的参数没有@CacheKey注解，那其他参数也不能有@CacheKey注解
        if (!parameterDetail.isAnnotationPresent(CacheKey.class)) {
            for (Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(CacheKey.class)) {
                    throw new IllegalStateException("The @CacheAsMulti parameter should has @CacheKey when other parameter has @CacheKey on " + method);
                }
            }
        }
    }

    @SneakyThrows
    protected static Object getCacheOperationField(JCacheOperation<?> operation, String fieldName) {
        Class<?> operationClass = operation.getClass();

        Field field = ReflectionUtils.findField(operationClass, fieldName);
        Objects.requireNonNull(field, "Invalid operation, not found " + fieldName + " on class " + operation.getClass().getName());
        field.setAccessible(true);

        return field.get(operation);
    }

}
