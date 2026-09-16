package io.github.ms100.cacheasmulti.cache.annotation;

import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author zhumengshuai
 */
public abstract class CacheAsMultiAnnotationUtils {

    @Nullable
    public static CacheAsMultiParameterDetail findAnnotation(Method method) {
        CacheAsMultiParameterDetail parameterDetail = null;
        int parameterCount = method.getParameterCount();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameterCount; i++) {
            Parameter parameter = parameters[i];
            if (!parameter.isAnnotationPresent(CacheAsMulti.class)) {
                continue;
            }
            // @CacheAsMulti注解只能存在一个
            if (parameterDetail != null) {
                throw new IllegalStateException("There can be only one @CacheAsMulti annotation in method parameters on " + method);
            }

            parameterDetail = new CacheAsMultiParameterDetail(method, i);
        }

        return parameterDetail;
    }
}
