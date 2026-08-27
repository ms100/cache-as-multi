# Outcome

关闭 Supervisor 最终验收中 A6、A8、A35、A40、A41 的实现与报告缺口：生产代码不再依赖 Java `assert`，JCache 内部字段缺失具有明确诊断，并完整披露仍未纳入修复的项目风险。

# Scope

- 将 sample `JCacheController.putFoo` 的 Java `assert` 替换为始终生效的显式运行时校验，并覆盖成功与不一致路径。
- 加固 `AbstractJCacheAsMultiOperation.getCacheOperationField`：字段不存在时抛出包含实际目标类型和字段名的 `IllegalStateException`。
- 为 JCache 字段解析失败补充回归测试，并确认现有 `keyParameterDetails` / `allParameterDetails` 正常路径保持可用。
- 对所有生产源码执行 Java `assert` 静态扫描。
- 在 Builder/Verifier 交付风险中明确报告 sample HTTP 请求路径的两次 10 秒固定等待，并保留 Spring 内部实现耦合、停止维护的依赖版本和历史 smoke coverage 风险。

# Non-goals

- 不移除或改写 `JCacheController.getFoo` 中的两次 `Thread.sleep(10000)`；本 child 只报告其阻塞请求线程的影响、证据和后续建议。
- 不升级 Java、Spring Boot、Spring Framework、Spring Data Redis 或其他依赖。
- 不重写基于 Spring JCache 内部实现和反射的整体集成架构。
- 不重开或修改四个已归档 child，不扩大为无关风格重构。

# Acceptance examples

- A1：JCache 必需内部字段不存在时抛出 `IllegalStateException`，消息同时包含实际 operation 类型与请求的字段名，不再产生 `NullPointerException`。
- A2：回归测试直接覆盖 JCache 字段缺失诊断，并确认正常字段解析与既有 JCache 批量缓存测试仍通过。
- A3：sample `putFoo` 在缓存返回期望值时保持正常完成；返回不一致时即使 JVM 未启用 `-ea` 也抛出包含上下文的显式 `IllegalStateException`。
- A4：核心库与 sample 的全部 `src/main` Java 源码不再包含用于必需成员、缓存值或不可达状态校验的 Java `assert`。
- A5：完整 Java 8 reactor 构建无需外部 Redis 并通过，既有 CompletionStage、缓存适配器与 sample 回归不受影响。
- A6：交付报告明确列出两次 10 秒请求线程等待的文件位置、运行影响和后续移除建议，并继续列出 Spring 内部耦合、过期依赖和 smoke coverage 风险；未修复事项不标记为已修复。
- A7：实现差异只包含上述诊断、显式校验、回归测试与正式产物，不改变公共缓存接口和正常批量语义。

# Constraints and invariants

- 保持 Java 8 与 Spring Boot 2.2 兼容。
- 显式校验必须在默认 JVM 配置下生效，错误消息应足以定位失败条件。
- 反射成员存在时保持现有访问结果；成员缺失时不得吞掉类型和成员上下文。
- 测试不得连接真实 Redis 或依赖墙钟等待。

# Decisions

- 本 child 严格继承 Supervisor 已确认的 A6、A8、A35、A40、A41，不引入新的用户可见范围。
- 用户确认用显式运行时校验替换 sample Java `assert`，并为 JCache 内部字段缺失补充明确异常和回归测试。
- 两次 10 秒请求路径等待属于已确认的非目标，只在最终风险报告中披露，不在本 child 删除。

# Open questions

- 无。

# Verification expectations

- 定向运行 JCache operation 字段解析和 sample controller 显式校验测试。
- 静态扫描所有 `src/main/**/*.java`，确认不存在生产 Java `assert`。
- 在 Corretto Java 8 下运行完整 reactor `clean package`，确认库、源码、Javadoc 与 sample 产物并验证无需外部服务。
- 检查 Builder handoff 与 Verifier 风险列表包含两次 10 秒请求等待及其后续建议。
