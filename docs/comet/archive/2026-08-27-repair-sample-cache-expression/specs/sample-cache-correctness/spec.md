# Sample 缓存表达式正确性

## Far 批量写入

- `FarService.putMultiFar2` 的 `@CachePut` key 表达式读取每个返回 `Far` 元素的 id 属性。
- 表达式不得调用不存在的历史 `Pair.getLeft()` 方法。
- 通过 Spring 缓存代理执行批量方法时不出现 SpEL 解析异常，并按 id 与 suffix 写入 `far2`。
- 后续单项查询能命中对应缓存值。

## 兼容性

- 仅校正表达式与增加回归测试，不改变 cache name、方法签名和返回结构。
