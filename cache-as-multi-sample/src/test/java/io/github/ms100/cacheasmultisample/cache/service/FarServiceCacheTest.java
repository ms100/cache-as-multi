package io.github.ms100.cacheasmultisample.cache.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = FarServiceCacheTest.TestApplication.class,
        properties = {
                "spring.cache.type=simple",
                "spring.main.web-application-type=none"
        }
)
class FarServiceCacheTest {

    @Autowired
    private FarService farService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        Cache cache = cacheManager.getCache("far2");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void putMultiFar2CachesEachFarByIdAndSuffix() {
        assertTrue(AopUtils.isAopProxy(farService));
        Set<Integer> ids = new LinkedHashSet<>(Arrays.asList(7, 9));
        String suffix = "sample";

        Map<Integer, Object> values = assertDoesNotThrow(
                () -> farService.putMultiFar2(ids, suffix)
        );

        Far firstExpected = (Far) values.get(7);
        Far secondExpected = (Far) values.get(9);
        assertSame(firstExpected, farService.getFar2(7, suffix));
        assertSame(secondExpected, farService.getFar2(9, suffix));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableCaching
    @Import(FarService.class)
    static class TestApplication {
    }
}
