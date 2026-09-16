package io.github.ms100.cacheasmulti.cache;

import io.github.ms100.cacheasmulti.cache.service.CompletionStageFarService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@SpringBootTest(properties = "spring.cache.type=simple")
class CompletionStageCacheAsMultiTest {

    @Autowired
    private CompletionStageFarService farService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        farService.clearAll();
        farService.resetCounters();
    }

    @Test
    void cacheMapByEachElementWithCompletionStage() {
        Set<Integer> ids = setOf(1, 2, 3);
        CompletionStage<Map<Integer, String>> firstStage = farService.getMultiFar(ids, "A");
        Map<Integer, String> first = firstStage.toCompletableFuture().join();
        Assertions.assertEquals(1, farService.getMapLoadCount());
        Assertions.assertEquals(3, first.size());

        Map<Integer, String> hit = farService.getMultiFar(ids, "A").toCompletableFuture().join();
        Assertions.assertEquals(first, hit);
        Assertions.assertEquals(1, farService.getMapLoadCount());

        Map<Integer, String> partial = farService.getMultiFar(setOf(2, 3, 4), "A").toCompletableFuture().join();
        Assertions.assertEquals(2, farService.getMapLoadCount());
        Assertions.assertEquals("id:4,name:4A", partial.get(4));
    }

    @Test
    void cacheListByEachElementWithCompletionStage() {
        List<Integer> ids = Arrays.asList(11, 12, 13);
        List<String> first = farService.getMultiFarList(ids, "L").toCompletableFuture().join();
        Assertions.assertEquals(Arrays.asList(
                "id:11,name:11L", "id:12,name:12L", "id:13,name:13L"), first);

        List<String> hit = farService.getMultiFarList(ids, "L").toCompletableFuture().join();
        Assertions.assertEquals(first, hit);
        Assertions.assertEquals(1, farService.getListLoadCount());

        List<String> partial = farService.getMultiFarList(Arrays.asList(12, 13, 14), "L")
                .toCompletableFuture().join();
        Assertions.assertEquals(Arrays.asList(
                "id:12,name:12L", "id:13,name:13L", "id:14,name:14L"), partial);
        Assertions.assertEquals(Arrays.asList(14), farService.getLastListIds());
        Assertions.assertEquals(2, farService.getListLoadCount());
    }

    @Test
    void cacheListWithAsElementFieldAndCompletionStage() {
        List<Integer> ids = Arrays.asList(1, 2, 3, 4);
        List<CompletionStageFarService.FarNode> first = farService
                .getMultiFarListAsElement(ids, "AE").toCompletableFuture().join();
        Assertions.assertEquals(Arrays.asList(1, 3), Arrays.asList(first.get(0).getId(), first.get(1).getId()));

        List<CompletionStageFarService.FarNode> hit = farService
                .getMultiFarListAsElement(ids, "AE").toCompletableFuture().join();
        Assertions.assertEquals(Arrays.asList(1, 3), Arrays.asList(hit.get(0).getId(), hit.get(1).getId()));
        Assertions.assertEquals(1, farService.getAsElementLoadCount());
    }

    @Test
    void strictNullKeepsMissingKeysAsMissForCompletionStage() {
        Set<Integer> ids = setOf(5, 6, 7, 8);
        Map<Integer, String> first = farService.getMultiFarStrict(ids, "S").toCompletableFuture().join();
        Assertions.assertEquals(setOf(5, 7), first.keySet());

        Map<Integer, String> second = farService.getMultiFarStrict(ids, "S").toCompletableFuture().join();
        Assertions.assertEquals(setOf(5, 7), second.keySet());
        Assertions.assertEquals(2, farService.getStrictLoadCount());
    }

    @Test
    void exceptionalCompletionStageDoesNotWriteCache() {
        Set<Integer> ids = setOf(1, 2);
        CompletionException first = Assertions.assertThrows(CompletionException.class,
                () -> farService.getExceptionalMultiFar(ids, "E").toCompletableFuture().join());
        Assertions.assertTrue(first.getCause() instanceof IllegalStateException);

        Assertions.assertThrows(CompletionException.class,
                () -> farService.getExceptionalMultiFar(ids, "E").toCompletableFuture().join());
        Assertions.assertEquals(2, farService.getExceptionalLoadCount());
    }

    @Test
    void canceledCompletionStageRemainsCanceledAndDoesNotWriteCache() {
        Set<Integer> ids = setOf(1, 2);
        CompletionStage<Map<Integer, String>> first = farService.getCanceledMultiFar(ids, "C");
        Assertions.assertTrue(first.toCompletableFuture().isCancelled());
        Assertions.assertThrows(CancellationException.class, () -> first.toCompletableFuture().join());

        CompletionStage<Map<Integer, String>> second = farService.getCanceledMultiFar(ids, "C");
        Assertions.assertTrue(second.toCompletableFuture().isCancelled());
        Assertions.assertThrows(CancellationException.class, () -> second.toCompletableFuture().join());
        Assertions.assertEquals(2, farService.getCanceledLoadCount());
    }

    @Test
    void canceledSourceIsDetectedWhenCallbackFailureIsWrappedDifferently() {
        Set<Integer> ids = setOf(11, 12);
        CompletionStage<Map<Integer, String>> stage = farService.getWrappedCanceledMultiFar(ids, "WC");

        Assertions.assertTrue(stage.toCompletableFuture().isCancelled());
        Assertions.assertThrows(CancellationException.class, () -> stage.toCompletableFuture().join());

        CompletionStage<Map<Integer, String>> second = farService.getWrappedCanceledMultiFar(ids, "WC");
        Assertions.assertTrue(second.toCompletableFuture().isCancelled());
        Assertions.assertEquals(2, farService.getWrappedCanceledLoadCount());
    }

    @Test
    void cachePutRunsOnlyAfterNormalCompletion() {
        Set<Integer> ids = setOf(21, 22);
        CompletionStage<Map<Integer, String>> stage = farService.putMultiFarDeferred(ids, "P");
        assertCacheMissing("stage-far-put", 21, "P");

        Map<Integer, String> values = valueMap(21, 22, "P");
        farService.completePendingPut(values);

        Assertions.assertEquals(values, stage.toCompletableFuture().join());
        assertCached("stage-far-put", 21, "P", values.get(21));
        assertCached("stage-far-put", 22, "P", values.get(22));
    }

    @Test
    void failedOrCanceledCachePutDoesNotWriteCache() {
        Set<Integer> ids = setOf(31, 32);
        CompletionStage<Map<Integer, String>> failed = farService.putMultiFarDeferred(ids, "F");
        farService.failPendingPut(new IllegalArgumentException("put-failed"));
        Assertions.assertThrows(CompletionException.class, () -> failed.toCompletableFuture().join());
        assertCacheMissing("stage-far-put", 31, "F");

        CompletionStage<Map<Integer, String>> canceled = farService.putMultiFarDeferred(ids, "C");
        farService.cancelPendingPut();
        Assertions.assertTrue(canceled.toCompletableFuture().isCancelled());
        assertCacheMissing("stage-far-put", 31, "C");
    }

    @Test
    void afterInvocationEvictionRunsOnlyAfterNormalCompletion() {
        Cache cache = requiredCache("stage-far-after-evict");
        Object key = new SimpleKey(41, "E");
        cache.put(key, "cached");

        CompletionStage<Map<Integer, String>> stage = farService.evictMultiFarDeferred(setOf(41), "E");
        Assertions.assertNotNull(cache.get(key));
        Map<Integer, String> values = valueMap(41, "E");
        farService.completePendingAfterEvict(values);

        Assertions.assertEquals(values, stage.toCompletableFuture().join());
        Assertions.assertNull(cache.get(key));
    }

    @Test
    void failedOrCanceledStageDoesNotRunAfterInvocationEviction() {
        Cache cache = requiredCache("stage-far-after-evict");
        Object failedKey = new SimpleKey(51, "F");
        cache.put(failedKey, "cached-failed");
        CompletionStage<Map<Integer, String>> failed = farService.evictMultiFarDeferred(setOf(51), "F");
        farService.failPendingAfterEvict(new IllegalStateException("evict-failed"));
        Assertions.assertThrows(CompletionException.class, () -> failed.toCompletableFuture().join());
        Assertions.assertEquals("cached-failed", cache.get(failedKey).get());

        Object canceledKey = new SimpleKey(52, "C");
        cache.put(canceledKey, "cached-canceled");
        CompletionStage<Map<Integer, String>> canceled = farService.evictMultiFarDeferred(setOf(52), "C");
        farService.cancelPendingAfterEvict();
        Assertions.assertTrue(canceled.toCompletableFuture().isCancelled());
        Assertions.assertEquals("cached-canceled", cache.get(canceledKey).get());
    }

    @Test
    void beforeInvocationEvictionRemainsImmediate() {
        Cache cache = requiredCache("stage-far-before-evict");
        Object key = new SimpleKey(61, "B");
        cache.put(key, "cached");

        CompletionStage<Map<Integer, String>> stage = farService
                .evictMultiFarBeforeInvocation(setOf(61), "B");

        Assertions.assertNull(cache.get(key));
        farService.cancelPendingBeforeEvict();
        Assertions.assertTrue(stage.toCompletableFuture().isCancelled());
    }

    @Test
    void synchronizedCacheableRejectsCompletionStageWithExplicitMessage() {
        IllegalStateException failure = Assertions.assertThrows(IllegalStateException.class,
                () -> farService.getMultiFarSynchronized(setOf(71), "S"));
        Assertions.assertTrue(failure.getMessage().contains("CompletionStage"));
    }

    private void assertCacheMissing(String cacheName, Integer id, String suffix) {
        Assertions.assertNull(requiredCache(cacheName).get(new SimpleKey(id, suffix)));
    }

    private void assertCached(String cacheName, Integer id, String suffix, String expected) {
        Assertions.assertEquals(expected, requiredCache(cacheName).get(new SimpleKey(id, suffix)).get());
    }

    private Cache requiredCache(String cacheName) {
        return java.util.Objects.requireNonNull(cacheManager.getCache(cacheName));
    }

    private static Map<Integer, String> valueMap(Integer id, String suffix) {
        return valueMap(new Integer[]{id}, suffix);
    }

    private static Map<Integer, String> valueMap(Integer first, Integer second, String suffix) {
        return valueMap(new Integer[]{first, second}, suffix);
    }

    private static Map<Integer, String> valueMap(Integer[] ids, String suffix) {
        return Arrays.stream(ids).collect(java.util.stream.Collectors.toMap(
                id -> id, id -> String.format("id:%d,name:%s%s", id, id, suffix)));
    }

    private static Set<Integer> setOf(Integer... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
