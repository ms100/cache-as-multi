package io.github.ms100.cacheasmulti.cache.config;

import io.github.ms100.cacheasmulti.support.TypeMethodKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Zhumengshuai
 */
@Component
public class MyKeyGenerator extends TypeMethodKeyGenerator {
    @Override
    protected String getMethod(Method method) {
        return "";
    }
}
