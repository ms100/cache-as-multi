package io.github.ms100.cacheasmulti.cache;

import io.github.ms100.cacheasmulti.cache.service.BoxService;
import io.github.ms100.cacheasmulti.cache.service.BusService;
import io.github.ms100.cacheasmulti.cache.service.FarService;
import io.github.ms100.cacheasmulti.cache.service.NewBarServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
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
    @Autowired
    private BoxService boxService;
    @Autowired
    private BusService busService;
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

    @Test
    void getMultiBox() {
        boxService.delMultiBox(ids);
        boxService.getMultiBox(ids);
        boxService.getMultiBox(ids);
        boxService.getMultiBox(ids);
        Set<Integer> ids2 = new HashSet<>(Arrays.asList(1, 3, 5, 7));
        boxService.delMultiBox(ids2);
        System.out.println(boxService.getMultiBox(ids2));
        System.out.println(boxService.getMultiBox(ids2));
    }

    @Test
    void putMultiBox() {
        boxService.delMultiBox(ids);
        boxService.putMultiBox(ids);
        boxService.getMultiBox(ids);
        Set<Integer> ids2 = new HashSet<>(Arrays.asList(1, 3, 5, 7));
        boxService.delMultiBox(ids2);
        System.out.println(boxService.putMultiBox(ids2));
        System.out.println(boxService.getMultiBox(ids2));
    }

    @Test
    void getMultiBox2() {
        boxService.delMultiBox2(ids, "a");
        boxService.getMultiBox2(ids, "a");
        boxService.getMultiBox2(ids, "a");
        boxService.getMultiBox2(ids, "a");
        Set<Integer> ids2 = new HashSet<>(Arrays.asList(1, 3, 5, 7));
        boxService.delMultiBox2(ids2, "a");
        System.out.println(boxService.getMultiBox2(ids2, "a"));
        System.out.println(boxService.getMultiBox2(ids2, "a"));
    }

    @Test
    void putMultiBox2() {
        boxService.delMultiBox2(ids, "a");
        boxService.putMultiBox2(ids, "a");
        boxService.getMultiBox2(ids, "a");
    }

    @Test
    void getBuss() {
        busService.deleteBuss(idList, "法拉利");
        List<BusService.Bus> buss = busService.getBuss(idList, "法拉利");
        System.out.println(buss);
        buss = busService.getBuss(idList, "法拉利");
        System.out.println(buss);
    }
}