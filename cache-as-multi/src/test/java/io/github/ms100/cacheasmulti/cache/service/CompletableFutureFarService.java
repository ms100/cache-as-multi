package io.github.ms100.cacheasmulti.cache.service;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CompletableFutureFarService {

    private final AtomicInteger mapLoadCount = new AtomicInteger();
    private final AtomicInteger listLoadCount = new AtomicInteger();
    private final AtomicInteger asElementLoadCount = new AtomicInteger();
    private final AtomicInteger strictLoadCount = new AtomicInteger();
    private final AtomicInteger exceptionalLoadCount = new AtomicInteger();
    private final AtomicInteger canceledLoadCount = new AtomicInteger();

    @Cacheable(cacheNames = "cf-far-map")
    public CompletableFuture<Map<Integer, String>> getMultiFar(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        mapLoadCount.incrementAndGet();
        return CompletableFuture.completedFuture(toValueMap(ids, suffix));
    }

    @Cacheable(cacheNames = "cf-far-list")
    public CompletableFuture<List<String>> getMultiFarList(
            @CacheAsMulti List<Integer> ids, String suffix) {
        listLoadCount.incrementAndGet();
        List<String> values = ids.stream()
                .map(id -> String.format("id:%d,name:%s%s", id, id, suffix))
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(values);
    }

    @Cacheable(cacheNames = "cf-far-list-as-element")
    public CompletableFuture<List<FarNode>> getMultiFarListAsElement(
            @CacheAsMulti(asElementField = "id") List<Integer> ids, String suffix) {
        asElementLoadCount.incrementAndGet();
        List<FarNode> values = ids.stream()
                .filter(id -> id % 2 == 1)
                .map(id -> new FarNode(id, String.format("id:%d,name:%s%s", id, id, suffix)))
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(values);
    }

    @Cacheable(cacheNames = "cf-far-strict")
    public CompletableFuture<Map<Integer, String>> getMultiFarStrict(
            @CacheAsMulti(strictNull = true) Set<Integer> ids, String suffix) {
        strictLoadCount.incrementAndGet();
        Map<Integer, String> values = ids.stream()
                .filter(id -> id % 2 == 1)
                .collect(Collectors.toMap(id -> id, id -> String.format("id:%d,name:%s%s", id, id, suffix)));
        return CompletableFuture.completedFuture(values);
    }

    @Cacheable(cacheNames = "cf-far-ex")
    public CompletableFuture<Map<Integer, String>> getExceptionalMultiFar(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        exceptionalLoadCount.incrementAndGet();
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new IllegalStateException("cf-exception-" + suffix));
        return future;
    }

    @Cacheable(cacheNames = "cf-far-cancel")
    public CompletableFuture<Map<Integer, String>> getCanceledMultiFar(
            @CacheAsMulti Set<Integer> ids, String suffix) {
        canceledLoadCount.incrementAndGet();
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        future.cancel(false);
        return future;
    }

    @CacheEvict(cacheNames = {
            "cf-far-map", "cf-far-list", "cf-far-list-as-element", "cf-far-strict", "cf-far-ex", "cf-far-cancel"
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
    }

    public int getMapLoadCount() {
        return mapLoadCount.get();
    }

    public int getListLoadCount() {
        return listLoadCount.get();
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

    private Map<Integer, String> toValueMap(Collection<Integer> ids, String suffix) {
        return ids.stream().collect(Collectors.toMap(
                id -> id,
                id -> String.format("id:%d,name:%s%s", id, id, suffix)
        ));
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
