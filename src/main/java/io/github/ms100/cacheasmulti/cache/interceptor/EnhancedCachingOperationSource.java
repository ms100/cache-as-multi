package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiAnnotationUtils;
import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhumengshuai
 */
public class EnhancedCachingOperationSource extends AnnotationCacheOperationSource {
    private final Map<Object, Collection<CacheAsMultiOperation<?>>> cacheAsMultiOperationsCache = new ConcurrentHashMap<>(1024);

    @Nullable
    public Collection<CacheAsMultiOperation<?>> getCacheAsMultiOperations(Method method, @Nullable Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        Object cacheKey = getCacheKey(method, targetClass);
        return cacheAsMultiOperationsCache.get(cacheKey);
    }

    @Override
    @Nullable
    protected Collection<CacheOperation> findCacheOperations(Class<?> clazz) {
        // 在findCacheOperations(Method method)里统一处理，这里返回null
        return null;
    }


    @Override
    @Nullable
    protected Collection<CacheOperation> findCacheOperations(Method method) {
        // 先找方法上的注解
        Collection<CacheOperation> cacheOperations = super.findCacheOperations(method);
        if (cacheOperations == null) {
            // 再找类上的注解
            cacheOperations = super.findCacheOperations(method.getDeclaringClass());
            if (cacheOperations == null || !ClassUtils.isUserLevelMethod(method)) {
                return null;
            }
        }

        CacheAsMultiParameterDetail parameterDetail = findCacheAsMultiParameter(
                method.getDeclaringClass(), method.getName(), method.getParameterTypes());
        
        if (parameterDetail == null) {
            return cacheOperations;
        }

        Collection<CacheAsMultiOperation<?>> cacheAsMultiOperations = new ArrayList<>(cacheOperations.size());
        cacheOperations.forEach(cacheOperation -> {
            CacheAsMultiOperation<?> cacheAsMultiOperation = createCacheAsMultiOperation(method, cacheOperation, parameterDetail);
            cacheAsMultiOperations.add(cacheAsMultiOperation);
        });
        Object cacheKey = getCacheKey(method, method.getDeclaringClass());
        cacheAsMultiOperationsCache.put(cacheKey, cacheAsMultiOperations);

        return cacheOperations;
    }

    protected CacheAsMultiOperation<?> createCacheAsMultiOperation(
            Method method, CacheOperation cacheOperation, CacheAsMultiParameterDetail parameterDetail) {

        return new CacheAsMultiOperation<>(method, cacheOperation, parameterDetail);
    }

    /**
     * 因为Spring的缓存注解可以子类继承父类，所以这里也需要逐层的查找@CacheAsMulti
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    @Nullable
    private CacheAsMultiParameterDetail findCacheAsMultiParameter(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        CacheAsMultiParameterDetail parameterDetail;
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            method = null;
        }

        if (method != null) {
            parameterDetail = CacheAsMultiAnnotationUtils.findAnnotation(method);
            if (parameterDetail != null) {
                return parameterDetail;
            }
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> interfaceClass : interfaces) {
                parameterDetail = findCacheAsMultiParameter(interfaceClass, methodName, parameterTypes);
                if (parameterDetail != null) {
                    return parameterDetail;
                }
            }
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            parameterDetail = findCacheAsMultiParameter(superclass, methodName, parameterTypes);
            if (parameterDetail != null) {
                return parameterDetail;
            }
        }

        return null;
    }
    
}