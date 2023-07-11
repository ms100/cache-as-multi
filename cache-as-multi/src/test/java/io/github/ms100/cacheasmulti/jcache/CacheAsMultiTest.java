package io.github.ms100.cacheasmulti.jcache;

import io.github.ms100.cacheasmulti.cache.service.FarService;
import io.github.ms100.cacheasmulti.jcache.service.BagService;
import io.github.ms100.cacheasmulti.jcache.service.CarService;
import io.github.ms100.cacheasmulti.jcache.service.DemoService;
import io.github.ms100.cacheasmulti.jcache.service.FooService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Zhumengshuai
 */
@SpringBootTest
class CacheAsMultiTest {
    @Autowired
    private FooService fooService;
    @Autowired
    private DemoService demoService;
    @Autowired
    private FarService farService;
    @Autowired
    private BagService bagService;
    @Autowired
    private CarService carService;
    private final Integer id = 12;

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
    @SneakyThrows
    void getFoo() {
        fooService.delMultiFoo(ids, "a", "b");
        fooService.getFoo(id, "a");
        System.out.println(fooService.getMultiFoo(ids, "a"));
        fooService.delFoo(id, "a", "b");
        System.out.println(fooService.getMultiFoo(ids, "a"));
        Thread.sleep(10000);
        System.out.println(fooService.getMultiFoo(ids, "a"));
        Thread.sleep(10000);
        System.out.println(fooService.getMultiFoo(ids, "a"));

    }

    @Test
    void evictFoo() {
        System.out.println(fooService.getMultiFoo(ids, "a"));
        fooService.delMultiFoo(ids, "a", "b");
        fooService.getFoo(id, "a");
        System.out.println(fooService.getMultiFoo(ids, "a"));
    }

    @Test
    void putFoo() {
        Map<Integer, String> map = new HashMap<>();
        ids.forEach(id -> {
            map.put(id, id.toString() + id);
        });
        fooService.putFoo(id, "a", "AAA");
        fooService.putMultiFoo(map, "a");
        String str = fooService.getFoo(id, "a");
        Assertions.assertEquals(id.toString() + id, str);
    }

    @Test
    void both() {
        fooService.delMultiFoo(ids, "a", "b");
        farService.delMultiFar(ids, "a");

        fooService.getMultiFoo(ids, "a");
        farService.getMultiFar(ids, "a");
    }

    @Test
    void putDemo() {
        Map<Integer, String> map = new HashMap<>();
        ids.forEach(id -> {
            map.put(id, id.toString() + id);
        });
        demoService.putDemo(id, "AAA");
        demoService.putMultiDemo(map);
    }

    @Test
    void getDemo() {
        demoService.delMultiDemo(idList, "a");
        demoService.getDemo(id, "a");
        System.out.println(demoService.getMultiDemo(idList, "a"));
    }

    @Test
    void getMultiBag() {
        bagService.delMultiBag(ids);
        bagService.getMultiBag(ids);
        bagService.getMultiBag(ids);
        bagService.getMultiBag(ids);
        Set<Integer> ids2 = new HashSet<>(Arrays.asList(1, 3, 5, 7));
        bagService.delMultiBag(ids2);
        System.out.println(bagService.getMultiBag(ids2));
        System.out.println(bagService.getMultiBag(ids2));
    }

    @Test
    void getMultiBag2() {
        bagService.delMultiBag2(ids, "a");
        bagService.getMultiBag2(ids, "a");
        bagService.getMultiBag2(ids, "a");
        bagService.getMultiBag2(ids, "a");
        Set<Integer> ids2 = new HashSet<>(Arrays.asList(1, 3, 5, 7));
        bagService.delMultiBag2(ids2, "a");
        System.out.println(bagService.getMultiBag2(ids2, "a"));
        System.out.println(bagService.getMultiBag2(ids2, "a"));
    }

    @Test
    void getCars() {
        carService.deleteCars(idList, "法拉利");
        List<CarService.Car> cars = carService.getCars(idList, "法拉利");
        System.out.println(cars);
        cars = carService.getCars(idList, "法拉利");
        System.out.println(cars);
    }
}