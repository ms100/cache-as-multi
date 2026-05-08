package io.github.ms100.cacheasmulti.cache;

import io.github.ms100.cacheasmulti.cache.service.CompletableFutureFarService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

@SpringBootTest
class CompletableFutureCacheAsMultiTest {

    @Autowired
    private CompletableFutureFarService farService;

    @BeforeEach
    void setUp() {
        farService.clearAll();
        farService.resetCounters();
    }

    @Test
    void cacheMapByEachElementWithCompletableFuture() {
        Set<Integer> ids = new HashSet<>(Arrays.asList(1, 2, 3));
        Map<Integer, String> first = farService.getMultiFar(ids, "A").join();
        Assertions.assertEquals(1, farService.getMapLoadCount());
        Assertions.assertEquals(3, first.size());

        Map<Integer, String> second = farService.getMultiFar(ids, "A").join();
        Assertions.assertEquals(first, second);
        Assertions.assertEquals(1, farService.getMapLoadCount());

        Set<Integer> partMissIds = new HashSet<>(Arrays.asList(2, 3, 4));
        Map<Integer, String> third = farService.getMultiFar(partMissIds, "A").join();
        Assertions.assertEquals(2, farService.getMapLoadCount());
        Assertions.assertEquals("id:4,name:4A", third.get(4));
    }

    @Test
    void cacheListByEachElementWithCompletableFuture() {
        List<Integer> ids = Arrays.asList(11, 12, 13);
        List<String> first = farService.getMultiFarList(ids, "L").join();
        Assertions.assertEquals(1, farService.getListLoadCount());
        Assertions.assertEquals(Arrays.asList("id:11,name:11L", "id:12,name:12L", "id:13,name:13L"), first);

        List<String> second = farService.getMultiFarList(ids, "L").join();
        Assertions.assertEquals(first, second);
        Assertions.assertEquals(1, farService.getListLoadCount());
    }

    @Test
    void cacheListWithAsElementFieldShouldAlsoWork() {
        List<Integer> ids = Arrays.asList(1, 2, 3, 4);
        List<CompletableFutureFarService.FarNode> first = farService.getMultiFarListAsElement(ids, "AE").join();
        Assertions.assertEquals(1, farService.getAsElementLoadCount());
        Assertions.assertEquals(2, first.size());
        Assertions.assertEquals(Arrays.asList(1, 3), Arrays.asList(first.get(0).getId(), first.get(1).getId()));

        List<CompletableFutureFarService.FarNode> second = farService.getMultiFarListAsElement(ids, "AE").join();
        Assertions.assertEquals(1, farService.getAsElementLoadCount());
        Assertions.assertEquals(2, second.size());
        Assertions.assertEquals(Arrays.asList(1, 3), Arrays.asList(second.get(0).getId(), second.get(1).getId()));
    }

    @Test
    void strictNullShouldKeepMissingKeysAsMiss() {
        Set<Integer> ids = new HashSet<>(Arrays.asList(5, 6, 7, 8));
        Map<Integer, String> first = farService.getMultiFarStrict(ids, "S").join();
        Assertions.assertEquals(1, farService.getStrictLoadCount());
        Assertions.assertEquals(new HashSet<>(Arrays.asList(5, 7)), first.keySet());

        Map<Integer, String> second = farService.getMultiFarStrict(ids, "S").join();
        Assertions.assertEquals(new HashSet<>(Arrays.asList(5, 7)), second.keySet());
        Assertions.assertEquals(2, farService.getStrictLoadCount());
    }

    @Test
    void exceptionalFutureShouldNotBeCached() {
        Set<Integer> ids = new HashSet<>(Arrays.asList(1, 2));
        CompletionException first = Assertions.assertThrows(
                CompletionException.class,
                () -> farService.getExceptionalMultiFar(ids, "E").join()
        );
        Assertions.assertTrue(first.getCause() instanceof IllegalStateException);

        CompletionException second = Assertions.assertThrows(
                CompletionException.class,
                () -> farService.getExceptionalMultiFar(ids, "E").join()
        );
        Assertions.assertTrue(second.getCause() instanceof IllegalStateException);
        Assertions.assertEquals(2, farService.getExceptionalLoadCount());
    }

    @Test
    void canceledFutureShouldNotBeCached() {
        Set<Integer> ids = new HashSet<>(Arrays.asList(1, 2));
        Assertions.assertThrows(CancellationException.class, () -> farService.getCanceledMultiFar(ids, "C").join());
        Assertions.assertThrows(CancellationException.class, () -> farService.getCanceledMultiFar(ids, "C").join());
        Assertions.assertEquals(2, farService.getCanceledLoadCount());
    }
}
