# 异步批量缓存

## 能力目标

当带有 `@CacheAsMulti` 的批量方法返回 `CompletionStage<Map<K,V>>` 或 `CompletionStage<List<V>>` 时，框架按集合元素执行缓存读取，并在异步计算正常完成后按元素执行缓存写入或 after-invocation eviction。该能力同时适用于 Spring Cache 与 JCache `@CacheResult`，并兼容具体返回类型 `CompletableFuture`。

## 返回类型识别

- 框架应以方法声明返回类型是否实现 `CompletionStage` 判断异步批量缓存，而不是只识别 `CompletableFuture`。
- 异步返回的第一个泛型参数必须继续满足既有 Map/List 批量返回约束。
- `CompletableFuture` 是受支持的 `CompletionStage` 实现，既有方法无需迁移即可继续工作。
- 非 `CompletionStage` 的 Map/List 返回类型继续走既有同步路径。

## 缓存读取与结果合并

- 全部元素命中缓存时，框架返回一个已正常完成的阶段，其值按既有 Map/List、顺序、`strictNull` 和 `asElementField` 规则构造。
- 部分元素未命中时，只以未命中元素调用原方法；原阶段正常完成后，将新结果与命中结果合并，再完成返回阶段。
- 全部元素未命中时，原阶段正常完成后写入各元素缓存；返回值保持原有批量结果语义。
- 处理过程不得通过 `get`、`join` 或其他方式阻塞调用线程等待结果。

## 完成、异常与取消

- Cacheable/CachePut 的缓存写入以及 after-invocation CacheEvict 仅在原阶段正常完成后执行。
- 原阶段异常完成或取消时，不执行依赖成功结果的缓存写入和 after-invocation eviction。
- 返回给调用方的组合阶段传播原失败；框架不吞掉异常，也不把失败结果当作普通缓存值。
- 当源阶段以取消结束时，返回阶段的 `toCompletableFuture()` 也应处于 cancelled 状态，`join()` 继续抛出 `CancellationException`，而不是变成包装该异常的普通 exceptional completion。
- before-invocation eviction 保持既有同步触发时机。

## Spring Cache 与 JCache

- Spring Cache 的 `@Cacheable`、`@CachePut`、`@CacheEvict` 组合继续遵守现有优先级、condition/unless 与元素级 key 语义。
- JCache `@CacheResult` 使用同样的 `CompletionStage` 识别与正常完成后写缓存语义。
- `@Cacheable(sync=true)` 不支持任何 `CompletionStage` 返回类型，并提供包含 `CompletionStage` 的明确错误信息。

## 文档与兼容性

- `@CacheAsMulti` Javadoc、中英文 README 和示例以 `CompletionStage` 描述公共能力，可使用 `CompletableFuture` 创建实际阶段。
- 用户已存在的 `CompletableFuture<Map<...>>` 与 `CompletableFuture<List<...>>` 方法继续受支持。
- 不改变同步 Map/List 方法和现有缓存后端的公共接口。

## 验证

- 测试覆盖 Map、List、全命中、部分命中、`strictNull`、`asElementField`、异常与取消路径。
