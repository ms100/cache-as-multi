package io.github.ms100.cacheasmulti.support;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.core.MethodClassKey;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 一个带类名#方法名的key生成器
 *
 * @author Zhumengshuai
 */
public class TypeMethodKeyGenerator implements KeyGenerator {

    private final ConcurrentMap<Object, String> ptmCache = new ConcurrentHashMap<>(1024);

    private static final int GET_A_OFFSET = SimpleKey.class.getSimpleName().length() + 1;

    @Override
    public Object generate(Object target, Method method, Object... params) {

        Class<?> targetClass = target.getClass();
        Object ptmCacheKey = getCacheKey(method, targetClass);

        String prefix = ptmCache.computeIfAbsent(ptmCacheKey, key -> computePrefix(targetClass, method));

        Object a = getParameters(params);
        return prefix + a;
    }

    protected String computePrefix(Class<?> targetClass, Method method) {
        String p = getPackage(targetClass);
        String t = getType(targetClass);
        String m = getMethod(method);
        return String.format("%s.%s#%s", p, t, m);
    }

    protected String getPackage(Class<?> targetClass) {

        String packageName = targetClass.getPackage().getName();
        StringJoiner stringJoiner = new StringJoiner(".");
        Arrays.stream(packageName.split("\\.")).map(s -> String.valueOf(s.charAt(0)))
                .forEach(stringJoiner::add);

        return stringJoiner.toString();
    }

    protected String getType(Class<?> targetClass) {
        return targetClass.getSimpleName();
    }

    protected String getMethod(Method method) {
        return method.getName();
    }

    protected String getParameters(Object... params) {
        Object key = SimpleKeyGenerator.generateKey(params);

        return key.toString().substring(GET_A_OFFSET);
    }

    protected Object getCacheKey(Method method, Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

}
