package io.github.ms100.cacheasmultisample.cache;

import io.github.ms100.cacheasmultisample.cache.service.FarService;
import io.github.ms100.cacheasmultisample.cache.service.NewBarServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Zhumengshuai
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cache")
class CacheController {
    private final NewBarServiceImpl barService;
    private final FarService farService;


    @GetMapping("getFar")
    public String getFar(@RequestParam("id") Integer id) {
        return farService.getFar(id, "a");
    }

    @GetMapping("putFar")
    public String putFar(@RequestParam("id") Integer id) {
        return farService.putFar(id, "a");
    }

    @GetMapping("delFar")
    public boolean delFar(@RequestParam("id") Integer id) {
        farService.delFar(id, "a");
        return true;
    }

    @GetMapping("getFars")
    public Map<Integer, String> getFars(@RequestParam("ids") String idsString) {
        Set<Integer> ids = Arrays.stream(idsString.split(",")).map(Integer::valueOf).collect(Collectors.toSet());
        return farService.getMultiFar(ids, "a");
    }

    @GetMapping("putFars")
    public Map<Integer, String> putFars(@RequestParam("ids") String idsString) {
        Set<Integer> ids = Arrays.stream(idsString.split(",")).map(Integer::valueOf).collect(Collectors.toSet());
        return farService.putMultiFar(ids, "a");
    }

    @GetMapping("delFars")
    public boolean delFars(@RequestParam("ids") String idsString) {
        Set<Integer> ids = Arrays.stream(idsString.split(",")).map(Integer::valueOf).collect(Collectors.toSet());
        farService.delMultiFar(ids, "a");
        return true;
    }

    @GetMapping("getBars")
    public Map<Integer, String> getBars(@RequestParam("ids") String idsString) {
        Set<Integer> ids = Arrays.stream(idsString.split(",")).map(Integer::valueOf).collect(Collectors.toSet());

        return barService.getMultiBar(ids, "b");
    }

    @GetMapping("delBars")
    public boolean delBars(@RequestParam("ids") String idsString) {
        Set<Integer> ids = Arrays.stream(idsString.split(",")).map(Integer::valueOf).collect(Collectors.toSet());

        barService.delMultiBar(ids, "b");
        return true;
    }

    @GetMapping("putBars")
    public Map<Integer, String> putBars(@RequestParam("ids") String idsString) {
        Set<Integer> ids = Arrays.stream(idsString.split(",")).map(Integer::valueOf).collect(Collectors.toSet());

        return barService.putMultiBar(ids, "b");
    }
}