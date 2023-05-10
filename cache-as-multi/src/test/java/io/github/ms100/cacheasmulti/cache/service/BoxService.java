package io.github.ms100.cacheasmulti.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BoxService {
    @Cacheable(cacheNames = "box")
    public Map<Integer, String> getMultiBox(@CacheAsMulti Set<Integer> ids) {
        System.out.println("getMultiBox====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 == 0).collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CachePut(cacheNames = "box")
    public Map<Integer, String> putMultiBox(@CacheAsMulti Set<Integer> ids) {
        System.out.println("putMultiBox====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 == 0).collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CacheEvict(cacheNames = "box", beforeInvocation = true)
    public void delMultiBox(@CacheAsMulti Set<Integer> ids) {
        System.out.println("delMultiBox====----" + ids.toString());
    }

    @Cacheable(cacheNames = "box")
    public Map<Integer, String> getMultiBox2(@CacheAsMulti(strictNull = true) Set<Integer> ids, String str) {
        System.out.println("getMultiBox2====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 == 0).collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CachePut(cacheNames = "box")
    public Map<Integer, String> putMultiBox2(@CacheAsMulti(strictNull = true) Set<Integer> ids, String str) {
        System.out.println("putMultiBox2====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 == 0).collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CacheEvict(cacheNames = "box", beforeInvocation = true)
    public void delMultiBox2(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("delMultiBox2====----" + ids.toString());
    }

}
