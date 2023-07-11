package io.github.ms100.cacheasmulti.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusService {
    @Cacheable(cacheNames = "bus")
    public List<Bus> getBuss(@CacheAsMulti(asElementField = "id") List<Integer> ids, String str) {
        System.out.println("getBuss====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 != 0).map(id -> new Bus(id, id + str)).collect(Collectors.toList());
    }

    @CacheEvict(cacheNames = "bus")
    public void deleteBuss(@CacheAsMulti List<Integer> ids, String str) {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Bus {
        private Integer id;
        private String name;
    }
}
