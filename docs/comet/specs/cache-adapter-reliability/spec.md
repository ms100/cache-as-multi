# 缓存适配器可靠性

## ConcurrentMap 批量写入

- `ConcurrentMapCache` 的批量写入必须对每个值调用与单条 `put` 一致的 `toStoreValue` 转换。
- 实际写入 native store 的必须是转换后的映射，不得在构造转换映射后仍写入原始输入。
- 允许 null 时，null 必须转换为 Spring 的内部 null holder，不能直接写入不允许 null 的 `ConcurrentHashMap`。
- store-by-value 模式继续使用源 cache 的序列化配置，批量读写语义与单条读写一致。

## 反射与异常传播

- 对 Spring 2.2 内部字段或方法的反射依赖在初始化时验证；成员缺失时抛出包含目标类型与成员名的明确 `IllegalStateException`。
- 生产代码不得依赖默认关闭的 Java `assert` 验证必需成员、必需值或不可达状态。
- Redis writer 的反射调用若由底层抛出运行时异常，应传播底层运行时异常或保留其直接 cause，不得只暴露 `InvocationTargetException` 导致代理转为 `UndeclaredThrowableException`。
- 加固不改变当前受支持缓存后端的公共接口和正常批量语义。

## 验证

- 回归测试覆盖 ConcurrentMap 的 null 值与 store-by-value 批量写入。
- 单元测试覆盖反射成员解析失败消息和反射调用底层异常展开，且不连接 Redis。
