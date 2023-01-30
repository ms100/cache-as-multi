package io.github.ms100.cacheasmulti.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BarServiceImpl implements BarService {
    @Override
    @Caching(
            //put = @CachePut(cacheNames = "bar1", key = "#a0"),
            cacheable = @Cacheable(cacheNames = "newBar", key = "#root.args[0]+' '+#str.toString()")
    )
    public Map<Integer, String> getMultiBar(@CacheAsMulti Set<Integer> ids, String str) {
        System.out.println("getBB====----" + ids.toString());
        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d", id)
        ));
    }
}
