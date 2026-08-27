package io.github.ms100.cacheasmultisample.jcache;

import io.github.ms100.cacheasmultisample.cache.service.FarService;
import io.github.ms100.cacheasmultisample.jcache.service.DemoService;
import io.github.ms100.cacheasmultisample.jcache.service.FooService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JCacheControllerTest {

    private FooService fooService;
    private JCacheController controller;

    @BeforeEach
    void setUp() {
        fooService = mock(FooService.class);
        controller = new JCacheController(
                fooService,
                mock(DemoService.class),
                mock(FarService.class));
    }

    @Test
    void putFooAcceptsExpectedCachedValue() {
        when(fooService.getFoo(12, "a")).thenReturn("1212");

        assertDoesNotThrow(controller::putFoo);
    }

    @Test
    void putFooRejectsUnexpectedCachedValueWithoutJavaAssertions() {
        when(fooService.getFoo(12, "a")).thenReturn("wrong");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                controller::putFoo);

        assertTrue(exception.getMessage().contains("1212"));
        assertTrue(exception.getMessage().contains("wrong"));
    }
}
