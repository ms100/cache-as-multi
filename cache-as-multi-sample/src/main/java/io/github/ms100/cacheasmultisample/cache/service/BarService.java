package io.github.ms100.cacheasmultisample.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.springframework.cache.annotation.CacheEvict;

import java.util.Map;
import java.util.Set;

public interface BarService {
    @CacheEvict(cacheNames = "ibar", key = "#ids+' '+#str+' '+#result")
    Map<Integer, String> getMultiBar(@CacheAsMulti Set<Integer> ids, String str);
}
