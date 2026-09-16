package io.github.ms100.cacheasmulti.jcache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.test.TestCompletionStages;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CompletionStageJCacheService {

    private final AtomicInteger mapLoadCount = new AtomicInteger();
    private final AtomicInteger listLoadCount = new AtomicInteger();
    private final AtomicInteger exceptionalLoadCount = new AtomicInteger();
    private final AtomicInteger canceledLoadCount = new AtomicInteger();
    private Collection<Integer> lastMapIds;
    private Collection<Integer> lastListIds;

    @CacheResult(cacheName = "stage-jcache-map")
    public CompletionStage<Map<Integer, String>> getMap(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        mapLoadCount.incrementAndGet();
        lastMapIds = ids;
        return completed(ids.stream().collect(Collectors.toMap(
                id -> id, id -> value(id, suffix))));
    }

    @CacheResult(cacheName = "stage-jcache-list")
    public CompletionStage<List<String>> getList(
            @CacheAsMulti List<Integer> ids, String suffix) {
        listLoadCount.incrementAndGet();
        lastListIds = ids;
        return completed(ids.stream().map(id -> value(id, suffix)).collect(Collectors.toList()));
    }

    @CacheResult(cacheName = "stage-jcache-exception")
    public CompletionStage<Map<Integer, String>> getExceptional(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        exceptionalLoadCount.incrementAndGet();
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new IllegalStateException("jcache-exception-" + suffix));
        return TestCompletionStages.hideCompletableFuture(future);
    }

    @CacheResult(cacheName = "stage-jcache-cancel")
    public CompletionStage<Map<Integer, String>> getCanceled(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        canceledLoadCount.incrementAndGet();
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        future.cancel(false);
        return TestCompletionStages.hideCompletableFuture(future);
    }

    public void reset() {
        mapLoadCount.set(0);
        listLoadCount.set(0);
        exceptionalLoadCount.set(0);
        canceledLoadCount.set(0);
        lastMapIds = null;
        lastListIds = null;
    }

    public int getMapLoadCount() {
        return mapLoadCount.get();
    }

    public int getListLoadCount() {
        return listLoadCount.get();
    }

    public int getExceptionalLoadCount() {
        return exceptionalLoadCount.get();
    }

    public int getCanceledLoadCount() {
        return canceledLoadCount.get();
    }

    public Collection<Integer> getLastMapIds() {
        return lastMapIds;
    }

    public Collection<Integer> getLastListIds() {
        return lastListIds;
    }

    private <T> CompletionStage<T> completed(T value) {
        return TestCompletionStages.hideCompletableFuture(CompletableFuture.completedFuture(value));
    }

    private String value(Integer id, String suffix) {
        return String.format("id:%d,name:%s%s", id, id, suffix);
    }
}
