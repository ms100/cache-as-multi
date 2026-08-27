package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Zhumengshuai
 */
class CacheAsMultiOperationTest {

    @Test
    void rejectsCompletionStageSubtypeThatCannotReceiveComposedFuture() throws Exception {
        Method method = TestService.class.getDeclaredMethod("load", Set.class);
        CacheAsMultiParameterDetail parameterDetail = new CacheAsMultiParameterDetail(method, 0);

        IllegalStateException failure = Assertions.assertThrows(IllegalStateException.class,
                () -> new TestOperation(method, parameterDetail));

        Assertions.assertTrue(failure.getMessage().contains("assignable from CompletableFuture"));
        Assertions.assertTrue(failure.getMessage().contains(method.toString()));
    }

    private interface CustomStage<T> extends CompletionStage<T> {
    }

    private static class TestService {
        @SuppressWarnings("unused")
        CustomStage<Map<Integer, String>> load(@CacheAsMulti Set<Integer> ids) {
            return null;
        }
    }

    private static class TestOperation extends AbstractCacheAsMultiOperation {
        TestOperation(Method method, CacheAsMultiParameterDetail parameterDetail) {
            super(method, parameterDetail);
        }
    }
}
