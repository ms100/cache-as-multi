package io.github.ms100.cacheasmulti.jcache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {
    @CacheResult(cacheName = "car")
    public List<Car> getCars(@CacheAsMulti(asElementField = "id") List<Integer> ids, String str) {
        System.out.println("getCars====----" + ids.toString());
        return ids.stream().filter(i -> i % 2 == 0).map(id -> new Car(id, id + str)).collect(Collectors.toList());
    }

    @CacheRemove(cacheName = "car")
    public void deleteCars(@CacheAsMulti List<Integer> ids, String str) {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Car {
        private Integer id;
        private String name;
    }
}
