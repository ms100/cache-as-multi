package io.github.ms100.cacheasmultisample.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "dog")
public class DogServiceImpl implements DogService {

    @Override
    @Cacheable
    public Dog getDog(Long dogId) {
        log.info("getDog: {}", dogId);
        return createDog(dogId);
    }

    @Override
    @Cacheable
    public Map<Long, Dog> getDogs(@CacheAsMulti List<Long> dogIds) {
        log.info("getDogs: {}", dogIds);
        Map<Long, Dog> res = new HashMap<>();
        for (Long dogId : dogIds) {
            res.put(dogId, createDog(dogId));
        }
        return res;
    }

    @Override
    @Cacheable
    public List<Dog> getDogList(@CacheAsMulti List<Long> dogIds) {
        log.info("getDogList: {}", dogIds);
        List<Dog> res = new ArrayList<>();
        for (Long dogId : dogIds) {
            res.add(createDog(dogId));
        }
        return res;
    }

    private Dog createDog(Long id) {
        Dog dog = new Dog();
        dog.setId(id);
        dog.setName("name" + id);

        return dog;
    }

}
