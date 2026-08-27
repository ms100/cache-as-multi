# 项目测试与构建质量

## 自包含测试

- 默认测试配置使用内存缓存，不连接本机或远程 Redis。
- Redis 专属序列化、TTL 或 writer 行为使用 mock/stub/单元边界验证；需要真实 Redis 的测试必须显式隔离，不能阻断默认 `mvn test`。
- Spring 测试代理方式与测试注入类型一致，完整上下文不因 JDK proxy/具体类类型不匹配而失败。

## 确定性与覆盖

- 默认测试不包含 10 秒级固定 `Thread.sleep`，不依赖墙钟等待 TTL。
- 测试使用 JUnit 断言而不是可能被 JVM 关闭的 Java `assert`。
- `TypeMethodKeyGenerator` 测试验证不同实际方法名产生不同前缀，并验证相同方法/参数的稳定性。
- Spring Cache 与 JCache 均有 CompletionStage 正常完成、异常完成与取消不写缓存的自动化覆盖。
- sample 有自动化测试执行 `putMultiFar2` 的 SpEL 键表达式，确保属性与返回元素类型匹配。

## 构建与仓库卫生

- 从干净输出开始运行完整 reactor 测试无需外部服务并通过。
- 完整 package 构建库 jar、source jar、Javadoc jar 与 sample；不要求发布凭据或签名材料。
- 已被 `.gitignore` 忽略的 `.DS_Store` 不再受版本控制。
- 仍需后续处理的架构风险和依赖版本风险在交付报告中列明，不用纯风格改动扩大本次 diff。
