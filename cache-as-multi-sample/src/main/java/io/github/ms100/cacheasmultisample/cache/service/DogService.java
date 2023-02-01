package io.github.ms100.cacheasmultisample.cache.service;

import java.util.List;
import java.util.Map;

/**
 * @author zhumengshuai
 */
public interface DogService {
    Dog getDog(Long dogId);

    Map<Long, Dog> getDogs(List<Long> dogIds);

    List<Dog> getDogList(List<Long> dogIds);

}
