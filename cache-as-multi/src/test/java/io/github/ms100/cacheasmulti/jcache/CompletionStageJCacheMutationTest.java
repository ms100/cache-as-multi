package io.github.ms100.cacheasmulti.jcache;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.test.TestCompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheValue;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.cache.type=simple")
@Import(CompletionStageJCacheMutationTest.Config.class)
@Timeout(5)
class CompletionStageJCacheMutationTest {

    @Autowired
    private MutationService service;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cache("mutation-put").clear();
        cache("mutation-remove").clear();
    }

    @Test
    void putWaitsForSuccessfulStageCompletion() {
        CompletionStage<?> result = service.put(Collections.singletonMap(1, "new"));
        assertFalse(result.toCompletableFuture().isDone());
        assertNull(cache("mutation-put").get(1));

        service.pendingPut().complete(null);

        assertNull(result.toCompletableFuture().join());
        assertEquals("new", cache("mutation-put").get(1, String.class));
    }

    @Test
    void removeWaitsForSuccessfulStageCompletion() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.remove(Collections.singleton(1));
        assertEquals("old", cache("mutation-remove").get(1, String.class));

        service.pendingRemove().complete(null);

        assertNull(result.toCompletableFuture().join());
        assertNull(cache("mutation-remove").get(1));
    }

    @Test
    void failedPutDoesNotWriteAndPreservesBusinessFailure() {
        CompletionStage<?> result = service.put(Collections.singletonMap(1, "new"));
        IllegalStateException failure = new IllegalStateException("put failed");
        service.pendingPut().completeExceptionally(failure);

        assertSame(failure, assertThrows(CompletionException.class,
                () -> result.toCompletableFuture().join()).getCause());
        assertNull(cache("mutation-put").get(1));
    }

    @Test
    void failedRemoveKeepsCachedValueAndPreservesBusinessFailure() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.remove(Collections.singleton(1));
        IllegalStateException failure = new IllegalStateException("remove failed");
        service.pendingRemove().completeExceptionally(failure);

        assertSame(failure, assertThrows(CompletionException.class,
                () -> result.toCompletableFuture().join()).getCause());
        assertEquals("old", cache("mutation-remove").get(1, String.class));
    }

    @Test
    void sourceCancellationDoesNotWrite() {
        CompletionStage<?> result = service.put(Collections.singletonMap(1, "new"));
        service.pendingPut().cancel(false);

        assertTrue(result.toCompletableFuture().isCancelled());
        assertNull(cache("mutation-put").get(1));
    }

    @Test
    void sourceCancellationDoesNotRemove() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.remove(Collections.singleton(1));
        service.pendingRemove().cancel(false);

        assertTrue(result.toCompletableFuture().isCancelled());
        assertEquals("old", cache("mutation-remove").get(1, String.class));
    }

    @Test
    void callerCancellationDoesNotWriteAfterSourceCompletes() {
        CompletionStage<?> result = service.put(Collections.singletonMap(1, "new"));
        assertTrue(result.toCompletableFuture().cancel(false));
        service.pendingPut().complete(null);

        assertTrue(result.toCompletableFuture().isCancelled());
        assertNull(cache("mutation-put").get(1));
    }

    @Test
    void callerCancellationDoesNotRemoveAfterSourceCompletes() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.remove(Collections.singleton(1));
        assertTrue(result.toCompletableFuture().cancel(false));
        service.pendingRemove().complete(null);

        assertTrue(result.toCompletableFuture().isCancelled());
        assertEquals("old", cache("mutation-remove").get(1, String.class));
    }

    @Test
    void putHonorsCacheForOnAsynchronousFailure() {
        CompletionStage<?> result = service.putWithExceptionRules(Collections.singletonMap(1, "new"));
        service.pendingPut().completeExceptionally(new CompletionException(new IllegalStateException("allowed")));

        assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
        assertEquals("new", cache("mutation-put").get(1, String.class));
    }

    @Test
    void putHonorsNoCacheForOnAsynchronousFailure() {
        CompletionStage<?> result = service.putWithExceptionRules(Collections.singletonMap(1, "new"));
        service.pendingPut().completeExceptionally(new IllegalArgumentException("excluded"));

        assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
        assertNull(cache("mutation-put").get(1));
    }

    @Test
    void removeHonorsEvictForOnAsynchronousFailure() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.removeWithExceptionRules(Collections.singleton(1));
        service.pendingRemove().completeExceptionally(new IllegalStateException("allowed"));

        assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
        assertNull(cache("mutation-remove").get(1));
    }

    @Test
    void removeHonorsNoEvictForOnAsynchronousFailure() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.removeWithExceptionRules(Collections.singleton(1));
        service.pendingRemove().completeExceptionally(new IllegalArgumentException("excluded"));

        assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
        assertEquals("old", cache("mutation-remove").get(1, String.class));
    }

    @Test
    void sourceCancellationSkipsExceptionConfiguredPut() {
        CompletionStage<?> result = service.putWithExceptionRules(Collections.singletonMap(1, "new"));
        service.pendingPut().cancel(false);

        assertTrue(result.toCompletableFuture().isCancelled());
        assertNull(cache("mutation-put").get(1));
    }

    @Test
    void sourceCancellationSkipsExceptionConfiguredRemove() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.removeWithExceptionRules(Collections.singleton(1));
        service.pendingRemove().cancel(false);

        assertTrue(result.toCompletableFuture().isCancelled());
        assertEquals("old", cache("mutation-remove").get(1, String.class));
    }

    @Test
    void earlyPutRemainsImmediateWhenStageFails() {
        CompletionStage<?> result = service.putBeforeInvocation(Collections.singletonMap(1, "new"));
        assertEquals("new", cache("mutation-put").get(1, String.class));
        service.pendingPut().completeExceptionally(new IllegalStateException("later failure"));

        assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
        assertEquals("new", cache("mutation-put").get(1, String.class));
    }

    @Test
    void earlyRemoveRemainsImmediateWhenStageIsCancelled() {
        cache("mutation-remove").put(1, "old");
        CompletionStage<?> result = service.removeBeforeInvocation(Collections.singleton(1));
        assertNull(cache("mutation-remove").get(1));
        service.pendingRemove().cancel(false);

        assertTrue(result.toCompletableFuture().isCancelled());
        assertNull(cache("mutation-remove").get(1));
    }

    private Cache cache(String name) {
        return java.util.Objects.requireNonNull(cacheManager.getCache(name));
    }

    @Test
    void defaultSynchronousPutFailureDoesNotWrite() {
        assertThrows(IllegalStateException.class,
                () -> service.failSynchronouslyOnPut(Collections.singletonMap(1, "new")));

        assertNull(cache("mutation-put").get(1));
    }

    @Test
    void defaultSynchronousRemoveFailureKeepsCachedValue() {
        cache("mutation-remove").put(1, "old");
        assertThrows(IllegalStateException.class,
                () -> service.failSynchronouslyOnRemove(Collections.singleton(1)));

        assertEquals("old", cache("mutation-remove").get(1, String.class));
    }

    @TestConfiguration
    static class Config {
        @Bean
        MutationService mutationService() {
            return new MutationService();
        }
    }

    public static class MutationService {
        private CompletableFuture<Void> put;
        private CompletableFuture<Void> remove;

        @CachePut(cacheName = "mutation-put")
        public CompletionStage<Void> put(@CacheAsMulti @CacheValue Map<Integer, String> values) {
            return newPutStage();
        }

        @CacheRemove(cacheName = "mutation-remove")
        public CompletionStage<Void> remove(@CacheAsMulti Set<Integer> ids) {
            return newRemoveStage();
        }

        @CachePut(cacheName = "mutation-put", cacheFor = RuntimeException.class,
                noCacheFor = IllegalArgumentException.class)
        public CompletionStage<Void> putWithExceptionRules(@CacheAsMulti @CacheValue Map<Integer, String> values) {
            return newPutStage();
        }

        @CacheRemove(cacheName = "mutation-remove", evictFor = RuntimeException.class,
                noEvictFor = IllegalArgumentException.class)
        public CompletionStage<Void> removeWithExceptionRules(@CacheAsMulti Set<Integer> ids) {
            return newRemoveStage();
        }

        @CachePut(cacheName = "mutation-put", afterInvocation = false)
        public CompletionStage<Void> putBeforeInvocation(@CacheAsMulti @CacheValue Map<Integer, String> values) {
            return newPutStage();
        }

        @CacheRemove(cacheName = "mutation-remove", afterInvocation = false)
        public CompletionStage<Void> removeBeforeInvocation(@CacheAsMulti Set<Integer> ids) {
            return newRemoveStage();
        }

        private CompletionStage<Void> newPutStage() {
            put = new CompletableFuture<>();
            return TestCompletionStages.hideCompletableFuture(put);
        }

        @CachePut(cacheName = "mutation-put")
        public void failSynchronouslyOnPut(@CacheAsMulti @CacheValue Map<Integer, String> values) {
            throw new IllegalStateException("synchronous put failure");
        }

        @CacheRemove(cacheName = "mutation-remove")
        public void failSynchronouslyOnRemove(@CacheAsMulti Set<Integer> ids) {
            throw new IllegalStateException("synchronous remove failure");
        }

        private CompletionStage<Void> newRemoveStage() {
            remove = new CompletableFuture<>();
            return TestCompletionStages.hideCompletableFuture(remove);
        }

        public CompletableFuture<Void> pendingPut() {
            return put;
        }

        public CompletableFuture<Void> pendingRemove() {
            return remove;
        }
    }
}
