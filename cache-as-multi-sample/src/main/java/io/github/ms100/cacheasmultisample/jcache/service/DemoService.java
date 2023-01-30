package io.github.ms100.cacheasmultisample.jcache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmultisample.jcache.support.FooKeyGenerator;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Zhumengshuai
 */
@Service
@CacheDefaults(cacheKeyGenerator = FooKeyGenerator.class)
public class DemoService {

    @CacheResult(cacheName = "demo")
    public String getDemo(@CacheKey Integer id, String str) {
        System.out.println("getDemo====----" + id);
        return String.format("id:%d,name:%s", id, id);
    }

    @CacheResult(cacheName = "demo")
    public List<String> getMultiDemo(@CacheAsMulti @CacheKey List<Integer> ids, String str) {
        System.out.println("getMultiDemo====----" + ids.toString());
        return ids.stream().map(id -> String.format("id:%d,name:%s", id, id)).collect(Collectors.toList());
    }


    @CachePut(cacheName = "demo")
    public void putDemo(@CacheKey Integer id, @CacheValue String value) {

    }

    @CachePut(cacheName = "demo")
    public void putMultiDemo(@CacheAsMulti @CacheValue Map<Integer, String> idValueMap) {

    }

    @CacheRemove(cacheName = "demo")
    public void delDemo(@CacheKey Integer id, String str) {

    }

    @CacheRemove(cacheName = "demo")
    public void delMultiDemo(@CacheAsMulti @CacheKey List<Integer> ids, String str) {
        System.out.println("delMultiDemo====----" + ids.toString());
    }
}