package io.github.ms100.cacheasmulti.jcache.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.cache.jcache.interceptor.JCacheOperation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AbstractJCacheAsMultiOperationTest {

    @Test
    void missingJCacheOperationFieldReportsTypeAndMember() {
        JCacheOperation<?> operation = mock(JCacheOperation.class);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> AbstractJCacheAsMultiOperation.getCacheOperationField(operation, "missingDetails"));

        assertTrue(exception.getMessage().contains(operation.getClass().getName()));
        assertTrue(exception.getMessage().contains("missingDetails"));
    }
}
