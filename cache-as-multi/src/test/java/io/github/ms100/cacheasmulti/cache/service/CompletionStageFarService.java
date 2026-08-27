package io.github.ms100.cacheasmulti.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.test.TestCompletionStages;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CompletionStageFarService {

    private final AtomicInteger mapLoadCount = new AtomicInteger();
    private final AtomicInteger listLoadCount = new AtomicInteger();
    private final AtomicInteger asElementLoadCount = new AtomicInteger();
    private final AtomicInteger strictLoadCount = new AtomicInteger();
    private final AtomicInteger exceptionalLoadCount = new AtomicInteger();
    private final AtomicInteger canceledLoadCount = new AtomicInteger();
    private final AtomicInteger wrappedCanceledLoadCount = new AtomicInteger();
    private CompletableFuture<Map<Integer, String>> pendingPut;
    private CompletableFuture<Map<Integer, String>> pendingAfterEvict;
    private CompletableFuture<Map<Integer, String>> pendingBeforeEvict;
    private List<Integer> lastListIds;

    @Cacheable(cacheNames = "stage-far-map")
    public CompletionStage<Map<Integer, String>> getMultiFar(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        mapLoadCount.incrementAndGet();
        return completed(toValueMap(ids, suffix));
    }

    @Cacheable(cacheNames = "stage-far-list")
    public CompletionStage<List<String>> getMultiFarList(
            @CacheAsMulti List<Integer> ids, String suffix) {
        listLoadCount.incrementAndGet();
        lastListIds = ids;
        List<String> values = ids.stream()
                .map(id -> value(id, suffix))
                .collect(Collectors.toList());
        return completed(values);
    }

    @Cacheable(cacheNames = "stage-far-list-as-element")
    public CompletionStage<List<FarNode>> getMultiFarListAsElement(
            @CacheAsMulti(asElementField = "id") List<Integer> ids, String suffix) {
        asElementLoadCount.incrementAndGet();
        List<FarNode> values = ids.stream()
                .filter(id -> id % 2 == 1)
                .map(id -> new FarNode(id, value(id, suffix)))
                .collect(Collectors.toList());
        return completed(values);
    }

    @Cacheable(cacheNames = "stage-far-strict")
    public CompletionStage<Map<Integer, String>> getMultiFarStrict(
            @CacheAsMulti(strictNull = true) Set<Integer> ids, String suffix) {
        strictLoadCount.incrementAndGet();
        Map<Integer, String> values = ids.stream()
                .filter(id -> id % 2 == 1)
                .collect(Collectors.toMap(id -> id, id -> value(id, suffix)));
        return completed(values);
    }

    @Cacheable(cacheNames = "stage-far-ex")
    public CompletionStage<Map<Integer, String>> getExceptionalMultiFar(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        exceptionalLoadCount.incrementAndGet();
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new IllegalStateException("stage-exception-" + suffix));
        return TestCompletionStages.hideCompletableFuture(future);
    }

    @Cacheable(cacheNames = "stage-far-cancel")
    public CompletionStage<Map<Integer, String>> getCanceledMultiFar(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        canceledLoadCount.incrementAndGet();
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        future.cancel(false);
        return TestCompletionStages.hideCompletableFuture(future);
    }

    @Cacheable(cacheNames = "stage-far-wrapped-cancel")
    public CompletionStage<Map<Integer, String>> getWrappedCanceledMultiFar(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        wrappedCanceledLoadCount.incrementAndGet();
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        future.cancel(false);
        return TestCompletionStages.hideCanceledFutureWithWrappedFailure(future);
    }

    @CachePut(cacheNames = "stage-far-put")
    public CompletionStage<Map<Integer, String>> putMultiFarDeferred(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        pendingPut = new CompletableFuture<>();
        return TestCompletionStages.hideCompletableFuture(pendingPut);
    }

    @CacheEvict(cacheNames = "stage-far-after-evict")
    public CompletionStage<Map<Integer, String>> evictMultiFarDeferred(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        pendingAfterEvict = new CompletableFuture<>();
        return TestCompletionStages.hideCompletableFuture(pendingAfterEvict);
    }

    @CacheEvict(cacheNames = "stage-far-before-evict", beforeInvocation = true)
    public CompletionStage<Map<Integer, String>> evictMultiFarBeforeInvocation(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        pendingBeforeEvict = new CompletableFuture<>();
        return TestCompletionStages.hideCompletableFuture(pendingBeforeEvict);
    }

    @Cacheable(cacheNames = "stage-far-sync", sync = true)
    public CompletionStage<Map<Integer, String>> getMultiFarSynchronized(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        return completed(toValueMap(ids, suffix));
    }

    @CacheEvict(cacheNames = {
            "stage-far-map", "stage-far-list", "stage-far-list-as-element",
            "stage-far-strict", "stage-far-ex", "stage-far-cancel", "stage-far-wrapped-cancel", "stage-far-put",
            "stage-far-after-evict", "stage-far-before-evict", "stage-far-sync"
    }, allEntries = true)
    public void clearAll() {
    }

    public void resetCounters() {
        mapLoadCount.set(0);
        listLoadCount.set(0);
        asElementLoadCount.set(0);
        strictLoadCount.set(0);
        exceptionalLoadCount.set(0);
        canceledLoadCount.set(0);
        wrappedCanceledLoadCount.set(0);
        pendingPut = null;
        pendingAfterEvict = null;
        pendingBeforeEvict = null;
        lastListIds = null;
    }

    public int getMapLoadCount() {
        return mapLoadCount.get();
    }

    public int getListLoadCount() {
        return listLoadCount.get();
    }

    public List<Integer> getLastListIds() {
        return lastListIds;
    }

    public int getAsElementLoadCount() {
        return asElementLoadCount.get();
    }

    public int getStrictLoadCount() {
        return strictLoadCount.get();
    }

    public int getExceptionalLoadCount() {
        return exceptionalLoadCount.get();
    }

    public int getCanceledLoadCount() {
        return canceledLoadCount.get();
    }

    public int getWrappedCanceledLoadCount() {
        return wrappedCanceledLoadCount.get();
    }

    public void completePendingPut(Map<Integer, String> values) {
        pendingPut.complete(values);
    }

    public void failPendingPut(Throwable failure) {
        pendingPut.completeExceptionally(failure);
    }

    public void cancelPendingPut() {
        pendingPut.cancel(false);
    }

    public void completePendingAfterEvict(Map<Integer, String> values) {
        pendingAfterEvict.complete(values);
    }

    public void failPendingAfterEvict(Throwable failure) {
        pendingAfterEvict.completeExceptionally(failure);
    }

    public void cancelPendingAfterEvict() {
        pendingAfterEvict.cancel(false);
    }

    public void cancelPendingBeforeEvict() {
        pendingBeforeEvict.cancel(false);
    }

    private <T> CompletionStage<T> completed(T value) {
        return TestCompletionStages.hideCompletableFuture(CompletableFuture.completedFuture(value));
    }

    private Map<Integer, String> toValueMap(Collection<Integer> ids, String suffix) {
        return ids.stream().collect(Collectors.toMap(id -> id, id -> value(id, suffix)));
    }

    private String value(Integer id, String suffix) {
        return String.format("id:%d,name:%s%s", id, id, suffix);
    }

    public static class FarNode {
        private final Integer id;
        private final String name;

        public FarNode(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
