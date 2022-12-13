package io.github.ms100.cacheasmulti.cache;

import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.Map;

/**
 * 对Spring的{@link Cache}的增强接口，增加了批量的方法
 * 注意与javax的{@link javax.cache.Cache}的区别
 *
 * @author zhumengshuai
 */
public interface EnhancedCache extends Cache {
    /**
     * 批量查询
     *
     * @param keys 缓存keys
     * @return key-value对
     */
    Map<Object, ValueWrapper> multiGet(Collection<?> keys);

    /**
     * 批量更新
     *
     * @param map key-value对
     */
    void multiPut(Map<?, ?> map);

    /**
     * 批量删除
     *
     * @param keys 缓存keys
     */
    void multiEvict(Collection<?> keys);
}
