# Outcome

将批量缓存的异步返回值支持从具体的 `CompletableFuture` 抽象为 `CompletionStage`，覆盖 Spring Cache 与 JCache 两条调用链；同时完成全项目实现审阅，修复用户确认纳入本次 change 的高置信度问题，并清晰报告其余改进建议。

# Scope

- 审阅 `cache-as-multi` 核心实现、自动配置、缓存适配器、测试与中英文文档。
- 使用 `CompletionStage` 识别异步方法、组合完成回调并构造全命中结果，不再要求运行时对象必须是 `CompletableFuture`。
- 保留现有 `CompletableFuture` 方法的兼容性，因为它实现 `CompletionStage`。
- 同步更新 Spring Cache、JCache、异常/取消、全命中/部分命中、Map/List 与 `sync=true` 校验路径。
- 更新新增异步能力对应的测试、注解文档和中英文 README。
- 修复已归档 review 验收确认的三个问题：异步取消状态丢失、ConcurrentMap 批量写入绕过值转换、sample 的失效 SpEL 键表达式。
- 治理测试基础设施，使完整 reactor 测试不依赖外部 Redis，并修复代理注入、错误断言、长时间 sleep 和缺失的 JCache 异步覆盖。
- 加固当前反射依赖的失败行为：必需成员缺失时清晰失败，反射调用不把底层运行时异常隐藏成 `InvocationTargetException`/`UndeclaredThrowableException`。
- 清理已被 `.gitignore` 忽略但仍受版本控制的 `.DS_Store`。

# Non-goals

- 不升级 Java、Spring Boot、Spring Framework 或第三方缓存依赖的大版本。
- 不重写基于 Spring 内部实现和反射的整体集成架构。
- 不改变同步批量缓存、键表达式、`strictNull`、`asElementField` 的既有对外语义。
- 不把演示代码全部改造成生产级测试；只治理会造成失败、外部环境依赖、超长运行或无法验证本次行为的部分。
- 不以本次治理为由改变 `TypeMethodKeyGenerator`“键包含实际方法名”的既有公共契约；修正与该契约相反的测试。

# Acceptance examples

- A1：声明返回 `CompletionStage<Map<K,V>>` 或 `CompletionStage<List<V>>` 的 Spring Cache 批量方法在全命中、全未命中和部分命中时均返回正确结果，并按元素读写缓存。
- A2：声明返回 `CompletionStage<Map<K,V>>` 或 `CompletionStage<List<V>>` 的 JCache `@CacheResult` 批量方法使用同样的异步完成语义。
- A3：异步阶段仅在正常完成后执行缓存写入与 after-invocation eviction；异常完成或取消不会产生缓存条目，异常通过返回阶段传播。
- A4：`@Cacheable(sync=true)` 与任意 `CompletionStage` 返回类型组合时仍在初始化/调用阶段给出明确拒绝信息。
- A5：现有声明为 `CompletableFuture` 的批量方法保持可用；文档和新增测试以更抽象的 `CompletionStage` 为主，并覆盖 Map/List、部分命中、`strictNull`、`asElementField`、异常与取消。
- A6：针对本次纳入范围的审阅发现提供回归测试；未纳入的发现按严重度、依据和建议方式报告，不伪装为已修复。
- A7：ConcurrentMap 批量写入实际使用经过 `toStoreValue` 转换的映射，允许 null 时不再向 `ConcurrentHashMap` 写入原始 null，并覆盖 store-by-value/null 回归测试。
- A8：生产代码不再用 Java `assert` 校验必需的 Spring 内部成员或缓存值；反射成员缺失给出明确异常，Redis 反射调用传播底层运行时原因而非无信息的包装异常。
- A9：sample 的 `putMultiFar2` 使用 `Far` 实际存在的 id 属性生成键，并有自动化测试触发该缓存表达式。
- A10：从干净构建开始运行完整 Maven reactor 测试时，无需本机 Redis 或其他外部服务即可通过。
- A11：历史测试不再包含 10 秒级固定等待；`TypeMethodKeyGenerator` 测试验证实际方法名隔离语义；异步测试覆盖 Spring Cache 与 JCache 的正常、异常及取消路径。
- A12：完整 reactor 可完成库、源码、Javadoc 与 sample 的编译打包；仓库不再跟踪 `.DS_Store`。

# Constraints and invariants

- 保持 Java 8 兼容。
- 保持异步方法“不支持 `@Cacheable(sync=true)`”的约束。
- 缓存副作用必须在异步计算正常完成后、返回的组合阶段完成前发生。
- 失败或取消的阶段不得写缓存；不得阻塞等待异步结果。
- 修改只限当前 change 的实现、测试、文档和 Native 正式产物。
- 完整测试默认使用内存缓存；Redis 专属行为通过不要求外部服务的单元测试或明确隔离的集成测试验证。

# Decisions

- Spring Cache 与 JCache 的新增异步路径都纳入 `CompletionStage` 抽象，避免两套语义分叉。
- `CompletableFuture` 作为 `CompletionStage` 的实现继续受支持，不做破坏性删除。
- 项目审阅采用“高置信度问题可修复、架构级/版本级问题报告后续建议”的分层方式。
- 用户选择扩大到全项目治理：本 change 修复已验证的功能缺陷、测试基础设施和错误传播问题，而不只输出建议。
- 归档 review 已独立验收三个仍存在的缺陷：`thenApply` 派生阶段丢失 cancelled 状态、ConcurrentMap 写错原始 map、sample 对 `Far` 调用不存在的 `getLeft()`。
- `TypeMethodKeyGenerator` 的实现与类注释都表明键包含方法名，因此历史测试的相等断言属于测试错误，而不是生产实现应删除方法隔离。
- 大型需求拆分为四个可独立验收的 child：先加固缓存适配器；随后推进 CompletionStage；sample 修复可独立进行；最后在集成结果上稳定完整测试与项目卫生。
- 用户已确认先实施第一波 `harden-cache-adapters` 与 `repair-sample-cache-expression`；第一波分别验收并合入 Supervisor 分支后，再按依赖进入后续波次。
- 用户已在阅读第一波缺陷、触发条件、影响和修复方向后授权开始实现；目标、范围、验收标准、关键约束与非目标均已确认。
- Supervisor 最终验收发现 A6、A8、A35、A40、A41 仍有遗漏；用户确认追加唯一 repair child `repair-supervisor-review-gaps`，只替换 sample 生产 Java `assert`、加固 JCache 内部字段缺失诊断并补全未纳入风险报告，不重开四个已归档 child，也不在本轮修复 sample 请求路径的两次 10 秒等待。

# Open questions

- 无。

# Verification expectations

- 使用项目 Maven 配置在 Java 8 目标级别完成 clean compile/test compile。
- 运行 CompletionStage 异步回归测试以及受改动缓存适配器的定向测试。
- 若选择修复测试隔离，则相关定向测试无需本机 Redis 即可通过。
- 检查生产代码、测试、注解 Javadoc 与中英文 README 中不再把支持范围错误限定为 `CompletableFuture`。
- 运行完整 reactor 的 clean test 与 package，确认 sample 和 Javadoc 一并构建。
- 对 Redis/反射加固定向运行不需要外部 Redis 的单元测试。
