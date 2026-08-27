# Outcome

修复 sample 中 `FarService.putMultiFar2` 沿用历史 Pair API 的失效缓存键表达式，使示例通过真实 Spring 缓存拦截调用时可以按 `Far.id` 正常写入和读取缓存。

# Scope

- 将 `putMultiFar2` 的 `#result.getLeft()` 改为读取 `Far` 实际公开的 id 属性。
- 添加 sample 自动化测试，通过 Spring 代理执行批量 `@CachePut` 并验证对应 `getFar2` 命中。

# Non-goals

- 不改变 cache name、方法参数、返回类型或 `Far` 数据模型。
- 不重构 sample 的日志、控制器或其他演示代码。
- 不修改核心缓存适配器或 CompletionStage 实现。

# Acceptance examples

- A1：`putMultiFar2` 的 key 表达式不再引用不存在的 `getLeft()`。
- A2：通过 Spring 代理调用 `putMultiFar2(ids, suffix)` 不出现 SpEL 方法/属性解析异常。
- A3：批量写入后调用 `getFar2(id, suffix)` 能读取由对应 `Far.id` 生成的缓存项，而不执行原方法。
- A4：修复不改变 `far2` cache name、方法签名或返回结构。

# Constraints and invariants

- 保持 Java 8 与 Spring Boot 2.2 兼容。
- 测试使用内存缓存，不要求 Redis。

# Decisions

- 该 child 严格继承 Supervisor 已确认的 A9、A58-A61 范围，无新增用户可见决定。
- 使用 SpEL 属性访问 `#result.id`，由 JavaBean getter `Far.getId()` 解析。

# Open questions

- 无。

# Verification expectations

- 运行 sample 的定向 Spring 缓存测试并确认表达式求值、批量写入及单项命中。
- 编译 sample 模块及其依赖的 `cache-as-multi` 模块。
