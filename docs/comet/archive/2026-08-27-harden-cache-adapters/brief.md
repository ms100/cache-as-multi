# Outcome

修复核心缓存适配器中批量写入绕过 Spring 值转换的问题，并使依赖 Spring 内部成员的反射失败能够清晰、直接地暴露真实原因。

# Scope

- `ConcurrentMapEnhancedCacheConverter` 批量写入使用转换后的值映射。
- 覆盖 allow-null 与 store-by-value 两种 `ConcurrentMapCache` 模式。
- 用显式状态校验替换生产代码中用于必需成员/必需值验证的 Java `assert`。
- 加固 ConcurrentMap、Redis 和 Redis customizer 反射成员解析与调用异常传播。
- 添加无需连接 Redis 的定向单元测试。

# Non-goals

- 不升级 Spring Boot/Spring Data Redis，不移除当前整体反射集成方式。
- 不修改 `EnhancedCache` 公共接口或同步批量缓存语义。
- 不处理 CompletionStage、sample 表达式或全量测试基础设施；它们由其他 child 覆盖。

# Acceptance examples

- A1：向允许 null 的 ConcurrentMap 增强缓存批量写入 `{key: null}` 不抛 `NullPointerException`，随后批量读取能识别该缓存值。
- A2：store-by-value 的批量写入与单条 `put` 一样存储序列化副本，而非原对象引用。
- A3：实际 native store 接收 `toStoreValue` 转换后的映射，不再接收原始输入 map。
- A4：生产代码不使用 Java `assert` 校验必需反射成员或待序列化缓存值。
- A5：必需的 Spring 内部字段/方法不存在时，初始化异常包含目标类型和成员名。
- A6：Redis writer 反射调用的底层运行时异常直接传播或成为直接 cause，不退化为无信息的 `InvocationTargetException`/`UndeclaredThrowableException`。
- A7：相关回归测试无需真实 Redis，且现有缓存适配器公共接口保持不变。

# Constraints and invariants

- 保持 Java 8 与 Spring Boot 2.2 兼容。
- null 和 store-by-value 的转换必须复用 Spring Cache 自身语义。
- 不吞掉底层异常，不用宽泛 catch 把所有失败改成同一种异常。

# Decisions

- 该 child 严格继承 Supervisor 已确认的 A7/A8、A36-A45 范围，无新增用户可见决定。
- ConcurrentMap 修复以写入已构造的 `newMap` 为核心，并通过行为测试防止回归。
- 反射加固保留现有兼容层，只改善初始化校验与 InvocationTargetException 展开。

# Open questions

- 无。

# Verification expectations

- 定向运行 ConcurrentMap converter 的 null/store-by-value 测试。
- 定向运行 Redis/反射异常传播测试，确认不发起网络连接。
- 编译 `cache-as-multi` 主源码与测试源码，并检查生产代码目标范围无 Java `assert`。
