package io.github.ms100.cacheasmulti.cache;

import io.github.ms100.cacheasmulti.cache.service.FarService;
import io.github.ms100.cacheasmulti.cache.service.NewBarServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Zhumengshuai
 */
@SpringBootTest
class CacheAsMultiTest {

    @Autowired
    private NewBarServiceImpl barService;

    @Autowired
    private FarService farService;

    private final Integer id = 3;

    private final List<Integer> idList = new ArrayList<Integer>() {{
        add(2);
        add(3);
        add(4);
        add(5);
        add(6);
        add(12);
    }};

    private final Set<Integer> ids = new HashSet<>(idList);

    @Test
    void getFar() {
        farService.delMultiFar(ids, "a");
        farService.getFar(id, "a");
        System.out.println(farService.getMultiFar(ids, "a"));
        farService.delFar(id, "a");
        System.out.println(farService.getMultiFar(ids, "a"));
    }

    @Test
    void evictFar() {
        farService.getMultiFar(ids, "a");
        farService.delMultiFar(ids, "a");
        farService.getMultiFar(ids, "a");
    }

    @Test
    void putFar() {
        farService.putFar(id, "a");
        String str = farService.getFar(id, "a");
        System.out.println(str);
        Assertions.assertEquals("AAA", str);
        farService.putMultiFar(ids, "a");
        str = farService.getFar(id, "a");
        System.out.println(str);
        Assertions.assertNotEquals("AAA", str);
    }

    @Test
    void putFar2() {
        farService.putFar2(id, "a");
        Object str = farService.getFar2(id, "a");
        System.out.println(str);
        Assertions.assertEquals("AAA", str);
        farService.putMultiFar2(ids, "a");
        str = farService.getFar2(id, "a");
        System.out.println(str);
        Assertions.assertNotEquals("AAA", str);
    }

    @Test
    void getFar2() {
        System.out.println(farService.getMultiFar3(idList, "a"));
        farService.delMultiFar3(idList, "a");
        farService.getFar(id, "a");
        System.out.println(farService.getMultiFar3(idList, "a"));
    }

    @Test
    void getBar() {
        barService.delMultiBar(ids, "b");
        barService.getBar(id, "b");
        System.out.println(barService.getMultiBar(ids, "b"));
        barService.delBar(id, "b");
        barService.getMultiBar(ids, "b");
    }

    @Test
    void evictBar() {
        barService.getMultiBar(ids, "b");
        barService.delMultiBar(ids, "b");
        barService.getMultiBar(ids, "b");
    }

    @Test
    void putBar() {
        barService.putBar(id, "b");
        String str = barService.getBar(id, "b");
        System.out.println(str);
        Assertions.assertEquals("BBB", str);
        barService.putMultiBar(ids, "b");
        str = barService.getBar(id, "b");
        System.out.println(str);
        Assertions.assertNotEquals("BBB", str);
    }
}