package io.github.ms100.cacheasmulti.support;

import io.github.ms100.cacheasmulti.support.TypeMethodKeyGenerator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Zhumengshuai
 */
class TypeMethodKeyGeneratorTest {

    @SneakyThrows
    @Test
    void generate() {
        TypeMethodKeyGenerator keyGenerator = new TypeMethodKeyGenerator();
        Method methodMulti = getClass().getDeclaredMethod("getMultiFoo");
        Object sm1 = keyGenerator.generate(this, methodMulti, "a", 1);
        Object sm2 = keyGenerator.generate(this, methodMulti, "a", "b", Arrays.asList(1, 2, 3));
        Method methodOne = getClass().getDeclaredMethod("getFoo");
        Object so1 = keyGenerator.generate(this, methodOne, "a", 1);
        Object so2 = keyGenerator.generate(this, methodOne, "a", "b", Arrays.asList(1, 2, 3));

        assertNotEquals(sm1, so1);
        assertNotEquals(sm2, so2);
        assertTrue(sm1.toString().contains("#getMultiFoo"));
        assertTrue(so1.toString().contains("#getFoo"));
        assertEquals(sm1, keyGenerator.generate(this, methodMulti, "a", 1));
        assertEquals(so1, keyGenerator.generate(this, methodOne, "a", 1));
    }

    void getMultiFoo(){

    }

    void getFoo(){

    }
}
