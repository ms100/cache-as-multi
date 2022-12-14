package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import lombok.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
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
    }

    public int getCacheAsMultiParameterPosition() {
        return parameterDetail.getPosition();
    }

    public Collection<?> newCacheAsMultiArg(Collection<?> subCacheAsMultiArg) {
        assert cacheAsMultiArgCreator != null;
        return cacheAsMultiArgCreator.apply(subCacheAsMultiArg);
    }

    @Nullable
    public Map<?, ?> makeCacheMap(Collection<?> subCacheAsMultiArg, @Nullable Object invokeValues) {
        if (returnTypeMaker == null) {
            return null;
        }

        return returnTypeMaker.makeCacheMap(subCacheAsMultiArg, invokeValues);
    }

    @Nullable
    public Object makeReturnObject(Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap) {
        if (returnTypeMaker == null) {
            return null;
        }

        return returnTypeMaker.makeReturnObject(cacheAsMultiArg, argValueMap);
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
    protected static ReturnTypeMaker initializeReturnTypeMaker(Method method, CacheAsMultiParameterDetail parameterDetail) {
        ResolvableType returnResolvableType = ResolvableType.forMethodReturnType(method);

        Class<?> returnType = returnResolvableType.toClass();
        // ?????????????????? Map????????? Map ??? key ?????????????????? collection ???????????????????????????
        if (Map.class.isAssignableFrom(returnType) && returnType.isAssignableFrom(HashMap.class)) {
            ResolvableType parameterResolvableType = ResolvableType.forMethodParameter(method,
                    parameterDetail.getPosition());

            Class<?> parameterGeneric = parameterResolvableType.resolveGeneric();
            Class<?> returnGeneric = returnResolvableType.resolveGeneric();

            if (parameterGeneric != returnGeneric) {
                throw new IllegalStateException("The key type of map returned is not same as @CacheAsMulti annotation parameter generic type on " + method);
            }
            return MapReturnTypeMaker.getInstance();
        }// ?????????????????? List??????????????????????????? List????????? List ??????????????? List ????????????????????????.
        else if (List.class.isAssignableFrom(returnType) && returnType.isAssignableFrom(ArrayList.class)) {
            if (!List.class.isAssignableFrom(parameterDetail.getRawType()) || !parameterDetail.getRawType().isAssignableFrom(ArrayList.class)) {
                throw new IllegalStateException("The @CacheAsMulti parameter type must be List when the return type is List on " + method);
            }
            return ListReturnTypeMaker.getInstance();
        }

        return null;
    }

    private interface ReturnTypeMaker {

        /**
         * ????????????miss?????????
         *
         * @param subCacheAsMultiArg ????????????
         * @param invokeValues       ??????????????????
         * @return ??????map
         */
        @Nullable
        Map<?, ?> makeCacheMap(Collection<?> subCacheAsMultiArg, @Nullable Object invokeValues);

        /**
         * ??????????????????
         *
         * @param cacheAsMultiArg ????????????
         * @param argValueMap     ?????????????????????????????????map
         * @return ????????????
         */
        @Nullable
        Object makeReturnObject(Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap);
    }

    private static class ListReturnTypeMaker implements ReturnTypeMaker {

        @Getter
        private static final ReturnTypeMaker instance = new ListReturnTypeMaker();

        @Override
        @Nullable
        public Map<?, ?> makeCacheMap(Collection<?> subCacheAsMultiArg, @Nullable Object invokeValues) {
            List<?> list = (List<?>) invokeValues;
            // ?????????????????? List????????? List ??????????????????????????? List ?????????
            if (list == null) {
                return null;
            }
            if (list.size() != subCacheAsMultiArg.size()) {
                throw new IllegalStateException("The size of return list is not equal to the size of parameter list");
            }
            Map<Object, Object> map = CollectionUtils.newHashMap(subCacheAsMultiArg.size());
            Iterator<?> iterator = list.iterator();
            subCacheAsMultiArg.forEach((argItem) -> {
                map.put(argItem, iterator.next());
            });
            return map;
        }

        @Override
        @Nullable
        public Object makeReturnObject(Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap) {
            List<Object> res = new ArrayList<>(cacheAsMultiArg.size());
            cacheAsMultiArg.forEach(argItem -> res.add(argValueMap.get(argItem)));
            return res;
        }
    }

    private static class MapReturnTypeMaker implements ReturnTypeMaker {

        @Getter
        private static final ReturnTypeMaker instance = new MapReturnTypeMaker();

        @Override
        @Nullable
        public Map<?, ?> makeCacheMap(Collection<?> subCacheAsMultiArg, @Nullable Object invokeValues) {
            if (invokeValues == null) {
                return null;
            }
            return (Map<?, ?>) invokeValues;
        }

        @Override
        @Nullable
        public Object makeReturnObject(Collection<?> cacheAsMultiArg, Map<?, ?> argValueMap) {
            return argValueMap;
        }
    }
}
