# Outcome

在前三个 child 已合入的集成基线上稳定项目测试、构建与仓库卫生，使完整 Maven reactor 在不依赖本机 Redis 或其他外部服务的前提下可重复通过，并为纳入范围的审阅问题提供回归证据、为剩余风险提供明确报告。

# Scope

- 默认测试配置改用内存缓存；Redis 专属行为改为 mock、stub 或单元边界验证，真实 Redis 测试显式隔离。
- 修复 Spring 测试中 JDK 代理与按具体实现类注入不匹配导致的上下文失败。
- 移除 10 秒级固定等待和测试中的 Java `assert`，改用确定性同步手段与 JUnit 断言。
- 修正 `TypeMethodKeyGenerator` 的历史错误期望，验证实际方法名隔离与相同输入稳定性。
- 核对 Spring Cache、JCache CompletionStage 正常、异常、取消路径及 sample `putMultiFar2` 表达式回归均纳入完整测试。
- 从干净输出运行完整 reactor test/package，覆盖库 jar、source jar、Javadoc jar 与 sample。
- 移除仍受 Git 跟踪的 `.DS_Store`。
- 汇总未纳入本次修复的架构风险和依赖版本风险，说明影响、证据和建议。

# Non-goals

- 不升级 Java、Spring Boot、Spring Framework 或缓存依赖的大版本。
- 不重写基于 Spring 内部实现和反射的整体集成架构。
- 不要求默认构建连接真实 Redis，也不新增需要发布凭据或签名材料的步骤。
- 不用纯风格改动、无关重构或演示代码全面生产化扩大 diff。
- 不改变 `TypeMethodKeyGenerator`“键包含实际方法名”的公共契约。

# Acceptance examples

- A1：针对本 child 纳入的审阅问题提供能够在修复前失败、修复后通过的回归测试；未纳入问题不伪装为已修复。
- A2：从干净构建开始运行完整 Maven reactor 测试时，无需本机 Redis 或其他外部服务即可通过。
- A3：历史测试不再包含 10 秒级固定等待；`TypeMethodKeyGenerator` 测试验证实际方法名隔离语义；异步测试覆盖 Spring Cache 与 JCache 的正常、异常及取消路径。
- A4：完整 reactor 可完成库、源码、Javadoc 与 sample 的编译打包；仓库不再跟踪 `.DS_Store`。
- A5：对纳入范围的审阅问题保留可验证的回归证据。
- A6：其余项目审阅发现说明影响、证据与建议，并明确标记为后续事项。

# Constraints and invariants

- 保持 Java 8 兼容。
- 保持前三个已归档 child 的功能与公共接口不回退。
- 默认测试必须自包含且可重复，不依赖墙钟长等待或外部服务。
- Redis 专属语义仍需通过无需真实 Redis 的自动化边界测试覆盖。
- 只修改测试稳定性、构建、仓库卫生及必要的配套文档，不扩展产品行为。

# Decisions

- 本 child 严格继承 Supervisor 已确认的 `stabilize-project-quality` 范围与验收覆盖，不重新引入产品行为决策。
- 默认测试采用内存缓存；真实 Redis 场景显式隔离，Redis 专属行为在 mock/stub 边界验证。
- 修复测试注入方式以匹配 JDK proxy，而不是改变生产代理策略。
- 保留 `TypeMethodKeyGenerator` 的方法名隔离语义并修正旧测试。
- 未纳入的架构和依赖风险进入交付报告，不通过顺手重构扩大范围。

# Open questions

- 无。

# Verification expectations

- 在 Java 8 目标级别从干净输出运行完整 Maven reactor test。
- 运行完整 Maven package，确认库、source、Javadoc 与 sample 产物全部生成且不需要签名/发布凭据。
- 定向运行被修改的代理注入、缓存配置、TTL/Redis 边界和 `TypeMethodKeyGenerator` 测试。
- 核对 Spring Cache、JCache CompletionStage 及 sample 表达式回归在完整测试中执行。
- 静态检查默认测试中不再有 10 秒级固定 `Thread.sleep`、Java `assert`，并确认 Git 不再跟踪 `.DS_Store`。
