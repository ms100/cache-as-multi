package io.github.ms100.cacheasmultisample.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NewBarServiceImpl extends BarServiceImpl {

    @Cacheable(cacheNames = "newBar", key = "#a0+' '+#str")
    public String getBar(Integer id, String str) {
        System.out.println("getBar====----" + id);
        return String.format("id:%d,name:%s", id, id);
    }

    @Override
    public Map<Integer, String> getMultiBar(Set<Integer> ids, String str) {
        System.out.println("getMultiBar====----" + ids.toString());
        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }

    @CacheEvict(cacheNames = "newBar", key = "#id+' '+#str")
    public void delBar(Integer id, String str) {
        System.out.println("delBar====----" + id);
    }

    @CacheEvict(cacheNames = "newBar", key = "#p0+' '+#p1")
    public void delMultiBar(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("delMultiBar====----" + ids.toString());
    }

    @CachePut(cacheNames = "newBar", key = "#a0+' '+#a1")
    public String putBar(Integer id, String str) {
        System.out.println("putBar====----" + id);

        return "BBB";
    }
    
    @CachePut(cacheNames = "newBar", key = "#ids+' '+#str")
    public Map<Integer, String> putMultiBar(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("putMultiBar====----" + ids.toString());

        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s", id, id)
        ));
    }
}
