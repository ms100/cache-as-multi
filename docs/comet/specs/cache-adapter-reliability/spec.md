# 缓存适配器可靠性

## ConcurrentMap 批量写入

- 每个输入值必须经过 `toStoreValue` 后再写入 native store。
- 允许 null 时使用 Spring 内部 null holder，不能把原始 null 传给 `ConcurrentHashMap`。
- store-by-value 时使用源缓存的序列化配置，批量写入与单条写入保持一致。

## 反射依赖

- 必需的内部字段和方法在初始化时显式验证，缺失异常包含类型与成员名。
- 生产代码不使用默认关闭的 Java `assert` 保证必需状态。
- 反射目标抛出的运行时异常保持为调用方可识别的根因，不能只暴露反射包装异常。

## 兼容性与测试

- 不改变 `EnhancedCache` 公共接口和正常批量语义。
- 单元测试覆盖 null、store-by-value、成员缺失诊断和底层异常展开，且不连接 Redis。
