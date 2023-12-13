package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import lombok.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zhumengshuai
 */
public abstract class AbstractCacheAsMultiOperation {

    protected final CacheAsMultiParameterDetail parameterDetail;

    @Nullable
    protected final Function<Collection<?>, Collection<?>> cacheAsMultiArgCreator;

    @Nullable
    protected final ReturnTypeMaker returnTypeMaker;

    public AbstractCacheAsMultiOperation(Method method, CacheAsMultiParameterDetail parameterDetail) {
        this.parameterDetail = parameterDetail;
        this.cacheAsMultiArgCreator = initializeCacheAsMultiArgCreator(parameterDetail);
        this.returnTypeMaker = initializeReturnTypeMaker(method, parameterDetail);
        validateParameterDetail(method, parameterDetail);
    }

    protected void validateParameterDetail(Method method, CacheAsMultiParameterDetail parameterDetail) {
        Class<?> rawType = parameterDetail.getRawType();
        // 面向接口，这里没有必要再增加更多的类型
        boolean isAllowedCollection = Collection.class.isAssignableFrom(rawType) &&
                (rawType.isAssignableFrom(ArrayList.class) || rawType.isAssignableFrom(HashSet.class));

        if (!isAllowedCollection) {
            throw new IllegalStateException("The @CacheAsMulti parameter should be assignable from ArrayList or HashSet, and assign to Collection on " + method);
        }
    }

    public int getCacheAsMultiParameterPosition() {
        return parameterDetail.getPosition();
    }

    public boolean isStrictNull() {
        return parameterDetail.isStrictNull();
    }

    public Collection<?> newCacheAsMultiArg(Collection<?> subCacheAsMultiArg) {
        assert cacheAsMultiArgCreator != null;
        return cacheAsMultiArgCreator.apply(subCacheAsMultiArg);
    }

    public Map<?, ?> makeCacheMap(Collection<?> subCacheAsMultiArg, @Nullable Object invokeValues) {
        if (returnTypeMaker == null) {
            return null;
        }

        return returnTypeMaker.makeCacheMap(parameterDetail, subCacheAsMultiArg, invokeValues);
    }

    @Nullable
    public Object makeReturnObject(Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap) {
        if (returnTypeMaker == null) {
            return null;
        }

        return returnTypeMaker.makeReturnObject(parameterDetail, cacheAsMultiArg, argValueMap);
    }

    @Nullable
    protected static Function<Collection<?>, Collection<?>> initializeCacheAsMultiArgCreator(CacheAsMultiParameterDetail parameterDetail) {
        Class<?> rawType = parameterDetail.getRawType();
        if (rawType.isAssignableFrom(ArrayList.class)) {
            return ArrayList::new;
        } else if (rawType.isAssignableFrom(HashSet.class)) {
            return HashSet::new;
        }
        return null;
    }

    @Nullable
    protected static ReturnTypeMaker<?> initializeReturnTypeMaker(Method method, CacheAsMultiParameterDetail parameterDetail) {
        ResolvableType returnResolvableType = ResolvableType.forMethodReturnType(method);

        Class<?> returnType = returnResolvableType.toClass();
        // 如果返回的是 Map，那么 Map 的 key 的类型应该与 collection 参数的泛型类型一致
        if (Map.class.isAssignableFrom(returnType) && returnType.isAssignableFrom(HashMap.class)) {
            ResolvableType parameterResolvableType = ResolvableType.forMethodParameter(method,
                    parameterDetail.getPosition());

            Class<?> parameterGeneric = parameterResolvableType.resolveGeneric();
            Class<?> returnGeneric = returnResolvableType.resolveGeneric();

            if (parameterGeneric != returnGeneric) {
                throw new IllegalStateException("The key type of map returned is not same as @CacheAsMulti annotation parameter generic type on " + method);
            }
            return MapReturnTypeMaker.getInstance();
        }// 如果返回的是 List，那么参数也应该是 List，结果 List 元素与参数 List 元素应该顺序对应.
        else if (List.class.isAssignableFrom(returnType) && returnType.isAssignableFrom(ArrayList.class)) {
            if (!List.class.isAssignableFrom(parameterDetail.getRawType()) || !parameterDetail.getRawType().isAssignableFrom(ArrayList.class)) {
                throw new IllegalStateException("The @CacheAsMulti parameter type must be List when the return type is List on " + method);
            }
            return ListReturnTypeMaker.getInstance();
        }

        return null;
    }

    private interface ReturnTypeMaker<T> {

        /**
         * 处理缓存miss的数据
         *
         * @param parameterDetail    参数详情
         * @param subCacheAsMultiArg 参数集合
         * @param invokeValues       方法返回结果
         * @return 缓存map
         */
        Map<?, ?> makeCacheMap(CacheAsMultiParameterDetail parameterDetail,
                               Collection<?> subCacheAsMultiArg, @Nullable T invokeValues);

        /**
         * 处理返回结果
         *
         * @param parameterDetail 参数详情
         * @param cacheAsMultiArg 参数集合
         * @param argValueMap     单个参数与其结果映射的map
         * @return 结果对象
         */
        @Nullable
        T makeReturnObject(CacheAsMultiParameterDetail parameterDetail,
                           Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap);
    }

    private static class ListReturnTypeMaker implements ReturnTypeMaker<List<?>> {
        private static final SimpleEvaluationContext.Builder CONTEXT_BUILDER = SimpleEvaluationContext.forReadOnlyDataBinding();
        @Getter
        private static final ReturnTypeMaker<List<?>> instance = new ListReturnTypeMaker();

        @Override
        public Map<?, ?> makeCacheMap(CacheAsMultiParameterDetail parameterDetail,
                                      Collection<?> subCacheAsMultiArg, @Nullable List<?> invokeValues) {
            if (CollectionUtils.isEmpty(invokeValues)) {
                if (parameterDetail.isStrictNull()) {
                    return Collections.emptyMap();
                }
                Map<Object, Object> map = new HashMap<>(subCacheAsMultiArg.size());
                for (Object o : subCacheAsMultiArg) {
                    map.put(o, null);
                }
                return map;
            }
            // 如果 asResult 为空，那么 List 的长度必须等于参数 List 的长度
            if (parameterDetail.getAsElementFieldExpression() == null) {
                if (invokeValues.size() != subCacheAsMultiArg.size()) {
                    throw new IllegalStateException("The size of return list is not equal to the size of parameter list");
                }
                Map<Object, Object> map = new HashMap<>(subCacheAsMultiArg.size());
                Iterator<?> iterator = invokeValues.iterator();
                subCacheAsMultiArg.forEach((argItem) -> {
                    map.put(argItem, iterator.next());
                });
                return map;
            }
            Map<Object, Object> map = new HashMap<>(subCacheAsMultiArg.size());
            invokeValues.forEach(i -> {
                SimpleEvaluationContext context = CONTEXT_BUILDER.withRootObject(i).build();
                map.put(parameterDetail.getAsElementFieldExpression().getValue(context), i);
            });
            if (!parameterDetail.isStrictNull() && map.size() != subCacheAsMultiArg.size()) {
                subCacheAsMultiArg.forEach((argItem) -> {
                    map.putIfAbsent(argItem, null);
                });
            }
            return map;
        }

        @Override
        @Nullable
        public List<?> makeReturnObject(CacheAsMultiParameterDetail parameterDetail,
                                        Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap) {
            List<Object> res = new ArrayList<>(cacheAsMultiArg.size());
            if (parameterDetail.getAsElementFieldExpression() == null || parameterDetail.isStrictNull()) {
                cacheAsMultiArg.forEach(argItem -> res.add(argValueMap.get(argItem)));
            } else {
                cacheAsMultiArg.forEach(argItem -> {
                    if (argValueMap.get(argItem) != null) {
                        res.add(argValueMap.get(argItem));
                    }
                });
            }
            return res;
        }
    }

    private static class MapReturnTypeMaker implements ReturnTypeMaker<Map<?, ?>> {

        @Getter
        private static final ReturnTypeMaker<Map<?, ?>> instance = new MapReturnTypeMaker();

        @Override
        public Map<?, ?> makeCacheMap(CacheAsMultiParameterDetail parameterDetail,
                                      Collection<?> subCacheAsMultiArg, @Nullable Map<?, ?> invokeValues) {
            if (CollectionUtils.isEmpty(invokeValues)) {
                if (parameterDetail.isStrictNull()) {
                    return Collections.emptyMap();
                }
                Map<Object, Object> map = new HashMap<>(subCacheAsMultiArg.size());
                for (Object o : subCacheAsMultiArg) {
                    map.put(o, null);
                }
                return map;
            }

            if (parameterDetail.isStrictNull() || invokeValues.size() == subCacheAsMultiArg.size()) {
                return invokeValues;
            }

            Map<Object, Object> map = new HashMap<>(subCacheAsMultiArg.size());
            for (Object o : subCacheAsMultiArg) {
                map.put(o, invokeValues.get(o));
            }
            return map;
        }

        @Override
        @Nullable
        public Map<?, ?> makeReturnObject(CacheAsMultiParameterDetail parameterDetail,
                                          Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap) {
            if (!parameterDetail.isStrictNull()) {
                argValueMap.entrySet().removeIf(e -> e.getValue() == null);
            }
            return argValueMap;
        }
    }
}
