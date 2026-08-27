# Sample 缓存表达式正确性

## Far 批量写入示例

- `FarService.putMultiFar2` 的 `@CachePut` key 表达式必须读取 `Far` 实际公开的 id 属性。
- 批量方法按每个返回元素计算 key 时不得调用不存在的历史 `Pair.getLeft()` API。
- sample 通过 Spring 缓存拦截器实际执行该方法时，不出现 SpEL 方法/属性解析异常，并能按 id 写入和读取对应缓存。

## 兼容性

- 修复仅校正失效表达式，不改变 sample 的 cache name、参数或返回类型。
