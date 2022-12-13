package io.github.ms100.cacheasmulti.jcache;

import io.github.ms100.cacheasmulti.cache.service.FarService;
import io.github.ms100.cacheasmulti.cache.jcache.service.DemoService;
import io.github.ms100.cacheasmulti.cache.jcache.service.FooService;
import io.github.ms100.cacheasmulti.paramsplitter.annotation.EnableParamSplitter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Zhumengshuai
 */
@SpringBootTest
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)
@EnableParamSplitter
class CacheAsMultiTest {
    @Autowired
    private FooService fooService;

    @Autowired
    private DemoService demoService;

    @Autowired
    private FarService farService;

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
}