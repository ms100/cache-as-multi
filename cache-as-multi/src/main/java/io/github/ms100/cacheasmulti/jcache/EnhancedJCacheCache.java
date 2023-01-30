package io.github.ms100.cacheasmulti.jcache;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import org.springframework.cache.jcache.JCacheCache;
import org.springframework.util.CollectionUtils;

import javax.cache.Cache;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * 对JSR-107的{@link Cache}进行包装，适配为{@link EnhancedCache}
 *
 * @author zhumengshuai
 */
public class EnhancedJCacheCache extends JCacheCache implements EnhancedCache {

    public EnhancedJCacheCache(Cache<Object, Object> jcache) {
        super(jcache);
    }

    public EnhancedJCacheCache(Cache<Object, Object> jcache, boolean allowNullValues) {
        super(jcache, allowNullValues);
    }

    @Override
    public Map<Object, ValueWrapper> multiGet(Collection<?> keys) {
        Map<Object, Object> map = getNativeCache().getAll(new HashSet<>(keys));

        Map<Object, ValueWrapper> newMap = CollectionUtils.newHashMap(keys.size());
        map.forEach((key, value) -> newMap.put(key, toValueWrapper(value)));

        return newMap;
    }

    @Override
    public void multiPut(Map<?, ?> map) {
        Map<Object, Object> newMap = CollectionUtils.newHashMap(map.size());
        map.forEach((key, value) -> newMap.put(key, toStoreValue(value)));

        getNativeCache().putAll(newMap);
    }

    @Override
    public void multiEvict(Collection<?> keys) {
        getNativeCache().removeAll(new HashSet<>(keys));
    }
}
