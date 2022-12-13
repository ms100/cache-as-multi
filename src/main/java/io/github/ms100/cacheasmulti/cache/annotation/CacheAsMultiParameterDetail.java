package io.github.ms100.cacheasmulti.cache.annotation;

import lombok.Getter;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * {@link CacheAsMulti @CacheAsMulti}注解的参数的详细信息
 *
 * @author Zhumengshuai
 */
@ToString
public class CacheAsMultiParameterDetail {

    /**
     * {@link CacheAsMulti @CacheAsMulti}注解的参数类型
     */
    @Getter
    private final Class<?> rawType;

    /**
     * {@link CacheAsMulti @CacheAsMulti}注解的参数位置
     */
    @Getter
    private final int position;

    /**
     * 参数上的全部注解
     */
    private final Annotation[] annotations;

    public CacheAsMultiParameterDetail(Method method, int position) {
        Parameter parameter = method.getParameters()[position];
        this.annotations = parameter.getAnnotations();
        this.rawType = parameter.getType();
        this.position = position;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

}