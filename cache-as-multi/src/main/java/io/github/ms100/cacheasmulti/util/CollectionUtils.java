package io.github.ms100.cacheasmulti.util;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionUtils {
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>((int) (expectedSize / DEFAULT_LOAD_FACTOR), DEFAULT_LOAD_FACTOR);
    }

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

}
