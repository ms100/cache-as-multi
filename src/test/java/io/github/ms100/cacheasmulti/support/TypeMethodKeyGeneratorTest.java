package io.github.ms100.cacheasmulti.support;

import io.github.ms100.cacheasmulti.support.TypeMethodKeyGenerator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

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
        System.out.println(sm1);
        Object sm2 = keyGenerator.generate(this, methodMulti, "a", "b", Arrays.asList(1, 2, 3));
        System.out.println(sm2);
        Method methodOne = getClass().getDeclaredMethod("getFoo");
        Object so1 = keyGenerator.generate(this,methodOne,"a",1);
        System.out.println(so1);
        Object so2 = keyGenerator.generate(this,methodOne,"a","b", Arrays.asList(1,2,3));
        System.out.println(so2);
        Assertions.assertEquals(sm1,so1);
        Assertions.assertEquals(sm2,so2);
    }

    void getMultiFoo(){

    }

    void getFoo(){

    }
}