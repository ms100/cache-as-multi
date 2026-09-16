package io.github.ms100.cacheasmulti.support.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisCacheCustomizerUtilsTest {

    @Test
    void missingConfigurationFieldIdentifiesTargetTypeAndMember() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> RedisCacheCustomizerUtils.getRequiredField(
                        String.class, "missingConfiguration", Object.class));

        assertTrue(thrown.getMessage().contains(String.class.getName()));
        assertTrue(thrown.getMessage().contains("missingConfiguration"));
    }
}
