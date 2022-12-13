package io.github.ms100.cacheasmulti.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.paramsplitter.annotation.SplitParam;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
//@CacheConfig(keyGenerator = "myKeyGenerator")
public class FarService {

    @Cacheable(cacheNames = "far")
    public String getFar(Integer id, String str) {
        System.out.println("getFar====----" + id);
        return String.format("id:%d,name:%s", id, id);
    }

    @Cacheable(cacheNames = "far")
    public Map<Integer, String> getMultiFar(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("getMultiFar====----" + ids.toString());
        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CacheEvict(cacheNames = "far")
    public void delFar(Integer id, String str) {
        System.out.println("delFar====----" + id);
    }

    @CacheEvict(cacheNames = "far", beforeInvocation = true)
    public void delMultiFar(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("delMultiFar====----" + ids.toString());
    }

    @CachePut(cacheNames = "far")
    public String putFar(Integer id, String str) {
        System.out.println("putFar====----" + id);

        return "AAA";
    }

    @CachePut(cacheNames = "far")
    public Map<Integer, String> putMultiFar(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("putMultiFar====----" + ids.toString());

        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }


    @Cacheable(cacheNames = "far2", key = "#a0+' '+#a1")
    public Object getFar2(Integer id, String str) {
        System.out.println("getFar2====----" + id);
        return Pair.of(id, String.format("id:%d,name:%s", id, id));
    }

    @CachePut(cacheNames = "far2", key = "#id+' '+#a1")
    public String putFar2(Integer id, String str) {
        System.out.println("putFar2====----" + id);

        return "AAA";
    }

    @CachePut(cacheNames = "far2", key = "#result.getLeft()+' '+#a1")
    public Map<Integer, Object> putMultiFar2(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("putMultiFar2====----" + ids.toString());

        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> Pair.of(id, String.format("id:%d,name:%s", id, id))
        ));
    }


    @Cacheable(cacheNames = "far")
    public List<String> getMultiFar3(@SplitParam(3) @CacheAsMulti List<Integer> ids, String str) {
        System.out.println("getMultiFar====----" + ids.toString());
        return ids.stream().map(id -> String.format("id:%d,name:%s", id, id)).collect(Collectors.toList());
    }

    @CacheEvict(cacheNames = "far")
    public void delMultiFar3(@SplitParam(4) @CacheAsMulti List<Integer> ids, String str) {
        System.out.println("delMultiFar====----" + ids.toString());
    }

}
