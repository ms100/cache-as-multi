package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CachePut;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnhancedCachingOperationSourceTest {

    @Test
    void resolvesBatchOperationsForJdkProxyInterfaceMethod() throws Exception {
        EnhancedCachingOperationSource source = new EnhancedCachingOperationSource();
        Method interfaceMethod = ProxyService.class.getMethod("putMulti", Set.class);

        assertNotNull(source.getCacheOperations(interfaceMethod, ProxyServiceImpl.class));
        Collection<CacheAsMultiOperation<?>> operations =
                source.getCacheAsMultiOperations(interfaceMethod, ProxyServiceImpl.class);

        assertNotNull(operations);
        assertFalse(operations.isEmpty());
    }

    interface ProxyService {
        Map<Integer, String> putMulti(Set<Integer> ids);
    }

    static class ProxyServiceImpl implements ProxyService {
        @Override
        @CachePut(cacheNames = "proxy", key = "#ids")
        public Map<Integer, String> putMulti(@CacheAsMulti Set<Integer> ids) {
            return Collections.emptyMap();
        }
    }
}
