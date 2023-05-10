package io.github.ms100.cacheasmulti.jcache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BagService {
    @CacheResult(cacheName = "bag")
    public Map<Integer, String> getMultiBag(@CacheAsMulti Set<Integer> ids) {
        System.out.println("getMultiBag====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 == 0).collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CacheRemove(cacheName = "bag", afterInvocation = false)
    public void delMultiBag(@CacheAsMulti Set<Integer> ids) {
        System.out.println("delMultiBag====----" + ids.toString());
    }

    @CacheResult(cacheName = "bag")
    public Map<Integer, String> getMultiBag2(@CacheAsMulti(strictNull = true) Set<Integer> ids, String str) {
        System.out.println("getMultiBag2====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 == 0).collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CacheRemove(cacheName = "bag", afterInvocation = false)
    public void delMultiBag2(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("delMultiBag2====----" + ids.toString());
    }

}
