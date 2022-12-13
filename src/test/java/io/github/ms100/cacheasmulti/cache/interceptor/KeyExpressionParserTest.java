package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.interceptor.CacheAsMultiOperation.KeyExpressionParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Zhumengshuai
 */
public class KeyExpressionParserTest {

    @Test
    void isCacheKey0() {
        KeyExpressionParser keyExpressionParser = new KeyExpressionParser("#p0");

        assertTrue(keyExpressionParser.containParameter(0));
        assertFalse(keyExpressionParser.containParameter("id"));
    }

    @Test
    void isCacheKey1() {
        KeyExpressionParser keyExpressionParser = new KeyExpressionParser("#a1+#root.args[2][3]+#name[0]");

        assertFalse(keyExpressionParser.containParameter("id"));
        assertTrue(keyExpressionParser.containParameter(1));
        assertTrue(keyExpressionParser.containParameter(2));
        assertFalse(keyExpressionParser.containParameter(3));
        assertTrue(keyExpressionParser.containParameter("name"));
    }

    @Test
    void isCacheKey2() {
        KeyExpressionParser keyExpressionParser = new KeyExpressionParser("#p0+#root.args[1].value+#name.first+result.code");

        assertTrue(keyExpressionParser.containParameter(0));
        assertTrue(keyExpressionParser.containParameter(1));
        assertFalse(keyExpressionParser.containParameter("args"));
        assertFalse(keyExpressionParser.containParameter("value"));
        assertTrue(keyExpressionParser.containParameter("name"));
        assertFalse(keyExpressionParser.containParameter("first"));
        assertFalse(keyExpressionParser.containParameter("result"));
        assertFalse(keyExpressionParser.containParameter("code"));
    }
}
