package io.github.ms100.cacheasmultisample.jcache;

import io.github.ms100.cacheasmultisample.cache.service.FarService;
import io.github.ms100.cacheasmultisample.jcache.service.DemoService;
import io.github.ms100.cacheasmultisample.jcache.service.FooService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Zhumengshuai
 */
@RestController
@RequestMapping("/jcache")
@RequiredArgsConstructor
class JCacheController {
    private final FooService fooService;
    private final DemoService demoService;
    private final FarService farService;

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

    @SneakyThrows
    @GetMapping("getFoo")
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

    @GetMapping("evictFoo")
    void evictFoo() {
        System.out.println(fooService.getMultiFoo(ids, "a"));
        fooService.delMultiFoo(ids, "a", "b");
        fooService.getFoo(id, "a");
        System.out.println(fooService.getMultiFoo(ids, "a"));
    }

    @GetMapping("putFoo")
    void putFoo() {
        Map<Integer, String> map = new HashMap<>();
        ids.forEach(id -> {
            map.put(id, id.toString() + id);
        });
        fooService.putFoo(id, "a", "AAA");
        fooService.putMultiFoo(map, "a");
        String str = fooService.getFoo(id, "a");
        assert (id.toString() + id).equals(str);
    }

    @GetMapping("both")
    void both() {
        fooService.delMultiFoo(ids, "a", "b");
        farService.delMultiFar(ids, "a");

        fooService.getMultiFoo(ids, "a");
        farService.getMultiFar(ids, "a");
    }

    @GetMapping("putDemo")
    void putDemo() {
        Map<Integer, String> map = new HashMap<>();
        ids.forEach(id -> {
            map.put(id, id.toString() + id);
        });
        demoService.putDemo(id, "AAA");
        demoService.putMultiDemo(map);
    }

    @GetMapping("getDemo")
    void getDemo() {
        demoService.delMultiDemo(idList, "a");
        demoService.getDemo(id, "a");
        System.out.println(demoService.getMultiDemo(idList, "a"));
    }
}