# Outcome

将批量缓存的异步返回值支持从具体的 `CompletableFuture` 抽象为 `CompletionStage`，统一 Spring Cache 与 JCache 的识别、组合、异常和取消语义，同时保持现有 `CompletableFuture` 方法兼容。

# Scope

- 以方法声明返回类型是否实现 `CompletionStage` 识别异步批量缓存方法。
- 支持 `CompletionStage<Map<K,V>>` 与 `CompletionStage<List<V>>` 的全命中、全未命中和部分命中路径。
- 在 Spring Cache 的 Cacheable、CachePut、CacheEvict 与 JCache `@CacheResult` 路径使用非阻塞阶段组合。
- 仅在源阶段正常完成后执行缓存写入与 after-invocation eviction，并保持异常与取消状态。
- 更新异步相关测试、`@CacheAsMulti` Javadoc、中英文 README 与示例说明。

# Non-goals

- 不改变同步 Map/List 方法、缓存键、`strictNull`、`asElementField` 或缓存后端公共接口。
- 不移除或弃用现有 `CompletableFuture` 用法。
- 不升级 Java、Spring Boot、Spring Framework 或第三方缓存依赖。
- 不处理完整测试基础设施、项目卫生或其余审阅建议；这些由 `stabilize-project-quality` child 覆盖。

# Acceptance examples

- A1：声明返回 `CompletionStage<Map<K,V>>` 或 `CompletionStage<List<V>>` 的 Spring Cache 批量方法在全命中、全未命中和部分命中时均返回正确结果，并按元素读写缓存。
- A2：声明返回 `CompletionStage<Map<K,V>>` 或 `CompletionStage<List<V>>` 的 JCache `@CacheResult` 批量方法使用同样的异步完成语义。
- A3：异步阶段仅在正常完成后执行缓存写入与 after-invocation eviction；异常完成或取消不会产生缓存条目，异常通过返回阶段传播。
- A4：`@Cacheable(sync=true)` 与任意 `CompletionStage` 返回类型组合时仍在初始化/调用阶段给出明确拒绝信息。
- A5：现有声明为 `CompletableFuture` 的批量方法保持可用；文档和新增测试以更抽象的 `CompletionStage` 为主，并覆盖 Map/List、部分命中、`strictNull`、`asElementField`、异常与取消。

# Constraints and invariants

- 保持 Java 8 与 Spring Boot 2.2 兼容。
- 缓存副作用必须在异步计算正常完成后、返回的组合阶段完成前发生。
- 失败或取消的阶段不得写缓存；不得调用 `get`、`join` 或其他阻塞等待方式。
- before-invocation eviction 保持既有同步触发时机。
- `@Cacheable(sync=true)` 继续拒绝所有异步批量返回类型。

# Decisions

- 该 child 严格继承 Supervisor 已确认的 A1-A5、A13-A33 范围，无新增用户可见决定。
- Spring Cache 与 JCache 使用同一 `CompletionStage` 抽象，避免两条调用链语义分叉。
- `CompletableFuture` 作为 `CompletionStage` 的实现继续受支持，不做破坏性迁移。
- 取消必须在返回阶段的 `toCompletableFuture()` 上保持 cancelled 状态，而不是退化为包装 `CancellationException` 的普通异常完成。

# Open questions

- 无。

# Verification expectations

- 定向测试 Spring Cache 与 JCache 的 CompletionStage Map/List 全命中、全未命中和部分命中路径。
- 覆盖 `strictNull`、`asElementField`、正常完成、异常完成和取消，不依赖墙钟等待。
- 验证失败/取消不写缓存、不执行 after-invocation eviction，before-invocation eviction 不变。
- 验证 `sync=true` 错误信息明确包含 `CompletionStage`，并回归现有 `CompletableFuture` 方法。
- 编译 Java 8 主源码、测试源码及 Javadoc，检查中英文文档不再把公共能力限定为 `CompletableFuture`。
