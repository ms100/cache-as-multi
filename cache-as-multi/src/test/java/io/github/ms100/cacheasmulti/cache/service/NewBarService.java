package io.github.ms100.cacheasmulti.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;

import java.util.Map;
import java.util.Set;

public interface NewBarService {

    String getBar(Integer id, String str);

    Map<Integer, String> getMultiBar(@CacheAsMulti Set<Integer> ids, String str);

    void delBar(Integer id, String str);

    void delMultiBar(@CacheAsMulti Set<Integer> ids, String str);

    String putBar(Integer id, String str);

    Map<Integer, String> putMultiBar(@CacheAsMulti Set<Integer> ids, String str);
}
