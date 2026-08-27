package io.github.ms100.cacheasmulti.jcache;

import io.github.ms100.cacheasmulti.jcache.service.CompletionStageJCacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@SpringBootTest(properties = "spring.cache.type=simple")
class CompletionStageJCacheAsMultiTest {

    private static final List<String> CACHE_NAMES = Arrays.asList(
            "stage-jcache-map", "stage-jcache-list",
            "stage-jcache-exception", "stage-jcache-cancel");

    @Autowired
    private CompletionStageJCacheService service;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        for (String cacheName : CACHE_NAMES) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        service.reset();
    }

    @Test
    void cacheResultSupportsCompletionStageMapFullHitAndPartialHit() {
        Map<Integer, String> first = service.getMap(setOf(1, 2, 3), "M")
                .toCompletableFuture().join();
        Assertions.assertEquals(3, first.size());
        Assertions.assertEquals(1, service.getMapLoadCount());

        Assertions.assertEquals(first, service.getMap(setOf(1, 2, 3), "M")
                .toCompletableFuture().join());
        Assertions.assertEquals(1, service.getMapLoadCount());

        Map<Integer, String> partial = service.getMap(setOf(2, 3, 4), "M")
                .toCompletableFuture().join();
        Assertions.assertEquals("id:4,name:4M", partial.get(4));
        Assertions.assertEquals(setOf(4), new HashSet<>(service.getLastMapIds()));
        Assertions.assertEquals(2, service.getMapLoadCount());
    }

    @Test
    void cacheResultSupportsCompletionStageListFullHitAndPartialHit() {
        List<String> first = service.getList(Arrays.asList(11, 12, 13), "L")
                .toCompletableFuture().join();
        Assertions.assertEquals(Arrays.asList(
                "id:11,name:11L", "id:12,name:12L", "id:13,name:13L"), first);

        Assertions.assertEquals(first, service.getList(Arrays.asList(11, 12, 13), "L")
                .toCompletableFuture().join());
        Assertions.assertEquals(1, service.getListLoadCount());

        List<String> partial = service.getList(Arrays.asList(12, 13, 14), "L")
                .toCompletableFuture().join();
        Assertions.assertEquals(Arrays.asList(
                "id:12,name:12L", "id:13,name:13L", "id:14,name:14L"), partial);
        Assertions.assertEquals(Arrays.asList(14), service.getLastListIds());
        Assertions.assertEquals(2, service.getListLoadCount());
    }

    @Test
    void exceptionalJCacheStageDoesNotWriteCache() {
        Set<Integer> ids = setOf(21, 22);
        CompletionException first = Assertions.assertThrows(CompletionException.class,
                () -> service.getExceptional(ids, "E").toCompletableFuture().join());
        Assertions.assertTrue(first.getCause() instanceof IllegalStateException);

        Assertions.assertThrows(CompletionException.class,
                () -> service.getExceptional(ids, "E").toCompletableFuture().join());
        Assertions.assertEquals(2, service.getExceptionalLoadCount());
    }

    @Test
    void canceledJCacheStageRemainsCanceledAndDoesNotWriteCache() {
        Set<Integer> ids = setOf(31, 32);
        CompletionStage<Map<Integer, String>> first = service.getCanceled(ids, "C");
        Assertions.assertTrue(first.toCompletableFuture().isCancelled());
        Assertions.assertThrows(CancellationException.class, () -> first.toCompletableFuture().join());

        CompletionStage<Map<Integer, String>> second = service.getCanceled(ids, "C");
        Assertions.assertTrue(second.toCompletableFuture().isCancelled());
        Assertions.assertEquals(2, service.getCanceledLoadCount());
    }

    private static Set<Integer> setOf(Integer... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
