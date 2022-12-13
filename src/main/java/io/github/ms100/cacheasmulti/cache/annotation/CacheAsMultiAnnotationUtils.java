package io.github.ms100.cacheasmulti.cache.annotation;

import org.springframework.lang.Nullable;

import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheValue;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * @author zhumengshuai
 */
public abstract class CacheAsMultiAnnotationUtils {

    @Nullable
    public static CacheAsMultiParameterDetail findAnnotation(Method method) {
        CacheAsMultiParameterDetail detail = null;
        int parameterCount = method.getParameterCount();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameterCount; i++) {
            Parameter parameter = parameters[i];
            if (!parameter.isAnnotationPresent(CacheAsMulti.class)) {
                continue;
            }
            // @CacheAsMulti注解只能存在一个
            if (detail != null) {
                throw new IllegalStateException("There can be only one @CacheAsMulti annotation in method parameters on " + method);
            }

            if (method.isAnnotationPresent(CachePut.class)) {
                // 如果方法被@CachePut注解，那@CacheAsMulti注解必须在@CacheValue注解的参数上
                if (!parameter.isAnnotationPresent(CacheValue.class)) {
                    throw new IllegalStateException("The @CacheAsMulti parameter should be same as @CacheValue on " + method);
                }
                Class<?> rawType = parameter.getType();
                // 参数必须是Map类型
                if (!Map.class.isAssignableFrom(rawType)) {
                    throw new IllegalStateException("The @CacheAsMulti parameter should be a map on " + method);
                }
            } else {
                Class<?> rawType = parameter.getType();
                // 面向接口，这里没有必要再增加更多的类型
                boolean isAllowedCollection = Collection.class.isAssignableFrom(rawType) &&
                        (rawType.isAssignableFrom(ArrayList.class) || rawType.isAssignableFrom(HashSet.class));

                if (!isAllowedCollection) {
                    throw new IllegalStateException("The @CacheAsMulti parameter should be assignable from ArrayList or HashSet, and assign to Collection on " + method);
                }
            }

            detail = new CacheAsMultiParameterDetail(method, i);
        }

        return detail;
    }
}
