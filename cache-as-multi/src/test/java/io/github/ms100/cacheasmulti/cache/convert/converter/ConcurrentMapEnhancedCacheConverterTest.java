package io.github.ms100.cacheasmulti.cache.convert.converter;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentMapEnhancedCacheConverterTest {

    private final ConcurrentMapEnhancedCacheConverter converter = new ConcurrentMapEnhancedCacheConverter();

    @Test
    void multiPutUsesNullHolderWhenNullValuesAreAllowed() {
        ConcurrentMap<Object, Object> store = new ConcurrentHashMap<>();
        ConcurrentMapCache source = new ConcurrentMapCache("allow-null", store, true);
        EnhancedCache cache = converter.convert(source);

        cache.multiPut(Collections.singletonMap("missing", null));

        assertNotNull(store.get("missing"));
        Map<Object, Cache.ValueWrapper> cachedValues = cache.multiGet(Collections.singleton("missing"));
        assertNotNull(cachedValues.get("missing"));
        assertNull(cachedValues.get("missing").get());
    }

    @Test
    void multiPutStoresSerializedCopyWhenStoreByValueIsEnabled() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setBeanClassLoader(getClass().getClassLoader());
        cacheManager.setStoreByValue(true);
        ConcurrentMapCache source = (ConcurrentMapCache) cacheManager.getCache("store-by-value");
        assertNotNull(source);
        EnhancedCache cache = converter.convert(source);
        List<String> original = new ArrayList<>();
        original.add("before");

        cache.multiPut(Collections.singletonMap("key", original));
        original.add("after");

        Object nativeValue = source.getNativeCache().get("key");
        assertNotSame(original, nativeValue);
        assertEquals(Collections.singletonList("before"), cache.get("key", List.class));
    }

    @Test
    void missingSerializationFieldIdentifiesTargetTypeAndMember() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> ConcurrentMapEnhancedCacheConverter.getRequiredDeclaredField(String.class, "missingField"));

        assertTrue(thrown.getMessage().contains(String.class.getName()));
        assertTrue(thrown.getMessage().contains("missingField"));
    }
}
