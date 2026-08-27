package io.github.ms100.cacheasmulti.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

public final class TestCompletionStages {

    private TestCompletionStages() {
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletionStage<T> hideCompletableFuture(CompletableFuture<T> future) {
        return (CompletionStage<T>) Proxy.newProxyInstance(
                CompletionStage.class.getClassLoader(),
                new Class<?>[]{CompletionStage.class},
                (proxy, method, args) -> {
                    try {
                        return method.invoke(future, args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletionStage<T> hideCanceledFutureWithWrappedFailure(CompletableFuture<T> future) {
        return (CompletionStage<T>) Proxy.newProxyInstance(
                CompletionStage.class.getClassLoader(),
                new Class<?>[]{CompletionStage.class},
                (proxy, method, args) -> {
                    if ("whenComplete".equals(method.getName())) {
                        BiConsumer<? super T, ? super Throwable> action =
                                (BiConsumer<? super T, ? super Throwable>) args[0];
                        return future.whenComplete((value, failure) -> action.accept(
                                value, new IllegalStateException("wrapped cancellation", failure)));
                    }
                    try {
                        return method.invoke(future, args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                });
    }
}
