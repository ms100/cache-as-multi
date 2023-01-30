package io.github.ms100.cacheasmulti.jcache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
//@CacheDefaults(cacheKeyGenerator = FooKeyGenerator.class)
public class FooService {

    @CacheResult(cacheName = "foo")
    public Map<Integer, String> getMultiFoo(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("getMultiFoo====----" + ids.toString());
        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CacheResult(cacheName = "foo")
    public String getFoo(Integer id, String str) {
        System.out.println("getFoo====----" + id);
        return String.format("id:%d,name:%s", id, id);
    }

    @CacheRemove(cacheName = "foo")
    public void delFoo(@CacheKey Integer id, @CacheKey String str, String str2) {
        System.out.println("delFoo====----" + id);
    }

    @CacheRemove(cacheName = "foo", afterInvocation = false)
    public void delMultiFoo(@CacheAsMulti @CacheKey Set<Integer> ids, @CacheKey String str, String str2) {
        System.out.println("delMultiFoo====----" + ids.toString());
    }

    @CachePut(cacheName = "foo")
    public void putFoo(Integer id, String str, @CacheValue String str2) {
        System.out.println("putFoo====----" + id);
    }

    @CachePut(cacheName = "foo")
    public void putMultiFoo(@CacheAsMulti @CacheValue Map<Integer, String> idStrMap, String str) {
        System.out.println("putMultiFoo====----" + idStrMap.toString());
    }
}
