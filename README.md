# CacheAsMulti

[In English](./README-EN.md)

## 安装
### Maven

```xml

<dependency>
  <groupId>io.github.ms100</groupId>
  <artifactId>cache-as-multi</artifactId>
  <version>1.2.0</version>
</dependency>
```


## 使用

本注解需要与下面两套注解搭配使用，以实现对被注解参数所在的方法进行批量的缓存操作。

* Spring的缓存注解 `@Cacheable`、`@CachePut`、`@CacheEvict`

* JSR-107的注解 `@CacheResult`、JSR-107的`@CachePut`、`@CacheRemove`、`@CacheKey`

> 只支持 PROXY 模式，不支持 ASPECTJ 模式

### @Cacheable 和 @CacheResult

#### 普通方法
假设已有获取单个对象的方法，如下：
```java
class FooService {
    public Foo getFoo(Integer fooId) {
      //...
    }
}

```

此时如果需要获取批量对象的方法，通常会是下面两种写法：
```java
class FooService {
    public Map<Integer, Foo> getMultiFoo(Collection<Integer> fooIds) {
        //...
    }
  
    public List<Foo> getMultiFoo(List<Integer> fooIds) {
        //...
    }
}
```

获取批量对象的方法相对于获取单个对象的方法会有两点变化：
1. 入参从单个对象(以下称【对象参数】)变为对象集合(以下称【对象集合参数】)，例如 `Integer` 变为 `Collection<Integer>` 或 `Set<Integer>` 或 `List<Integer>`。
2. 返回值从单个对象变为 `Map<K,V>` 或者 `List<V>` 。例如 `Map<Integer,Foo>` 或 `List<Foo>`，若返回的是 `List` 类型，那应与【对象集合参数】大小相同并顺序一致。

#### 加缓存
在上面例子中，如果需要对获取单个对象的方法做缓存，会使用 `@Cacheable` 或 `@CacheResult` 注解： (PS: 这里将 `@CacheResult` 和 `@Cacheable` 放在一起举例子，实际使用时通常只用其中的一个)
```java
class FooService {
    @Cacheable(cacheNames = "foo")
    @CacheResult(cacheName = "foo")
    public Foo getFoo(Integer fooId) {
        // 用 fooId 生成缓存 key 和计算 condition、unless 条件，用 Foo 为缓存值
    }
}
```

如果对获取批量对象的方法直接加上 `@Cacheable` 或 `@CacheResult`，则会使用【对象集合参数】整体生成一个缓存 key，将返回的 `Map` 或 `List` 整体作为一个缓存值。

但通常我们会希望它能变为多个 `fooId => Foo` 的缓存，即：使用【对象集合参数】中每个【元素】和它对应的值分别作缓存。**此时只需要在【对象集合参数】上加上 @CacheAsMulti 注解即可实现我们想要的缓存方式。**
```java
class FooService {
    @Cacheable(cacheNames = "foo")
    @CacheResult(cacheName = "foo")
    public Map<Integer, Foo> getMultiFoo(@CacheAsMulti Collection<Integer> fooIds) {
      // 为 fooIds 集合中每个元素分别生成缓存 key 和计算 condition、unless 条件，用 Map 中对应的值作为缓存值
    }
  
    @Cacheable(cacheNames = "foo")
    @CacheResult(cacheName = "foo")
    public List<Foo> getMultiFoo(@CacheAsMulti List<Integer> fooIds) {
      // 为 fooIds 集合中每个元素分别生成缓存 key 和计算 condition、unless 条件，用 List 中对应的值作为缓存值
      // 之后的例子中，返回 List 和 返回 Map 的处理方式都一样，就不再单独举例
    }
}
```

#### 当方法有多个参数时
示例如下：

* 使用 `@Cacheable` 时 `@Cacheable.key()` 未配置【或】使用 `@CacheResult` 时参数中没有 `@CacheKey`
  ```java
  class FooService {
      @Cacheable(cacheNames = "foo", key="")
      @CacheResult(cacheName = "foo")
      public Foo getFoo(Integer fooId, String arg1) {
          // 用 fooId 和 arg1 两个参数生成缓存的 key，用返回值作为缓存值
      }
  
      @Cacheable(cacheNames = "foo", key="")
      @CacheResult(cacheName = "foo")
      public Map<Integer, Foo> getMultiFoo(@CacheAsMulti Collection<Integer> fooIds, String arg1) {
          // 用 fooIds 中的每个【元素】分别和 arg1 参数生成缓存的 key，用返回 Map 中【元素】对应的值作为缓存值
      }
  }
  ```

* 使用 `@Cacheable` 时 `@Cacheable.key()` 只配置了【对象参数】的引用【或】使用 `@CacheResult` 时只有【对象参数】上有 `@CacheKey`
  ```java
  class FooService {
      @Cacheable(cacheNames = "foo", key="#fooId")
      @CacheResult(cacheName = "foo")
      public Foo getFoo(@CacheKey Integer fooId, String arg1) {
          // 用 fooId 生成缓存的 key，用返回值作为缓存值
      }
  
      @Cacheable(cacheNames = "foo", key="#fooIds")
      @CacheResult(cacheName = "foo")
      public Map<Integer, Foo> getMultiFoo(@CacheAsMulti @CacheKey Collection<Integer> fooIds, String arg1) {
          // 用 fooIds 中的每个【元素】分别生成缓存的 key，用返回 Map 中【元素】对应的值作为缓存值
      }
  }
  ```

* 使用 `@Cacheable` 时 `@Cacheable.key()` 配置了若干参数的引用【或】使用 `@CacheResult` 时参数中有若干 `@CacheKey`
  ```java
  class FooService {
      @Cacheable(cacheNames = "foo", key="#fooId+#arg1")
      @CacheResult(cacheName = "foo")
      public Foo getFoo(@CacheKey Integer fooId, @CacheKey String arg1,  Float arg2) {
          // 用 fooId 和 arg1 两个参数生成缓存的 key，用返回值作为缓存值
      }
  
      @Cacheable(cacheNames = "foo", key="#fooIds+#arg1")
      @CacheResult(cacheName = "foo")
      public Map<Integer, Foo> getMultiFoo(@CacheAsMulti @CacheKey Collection<Integer> fooIds, @CacheKey String arg1,  Float arg2) {
          // 用 fooIds 中的每个【元素】分别和 arg1 参数生成缓存的 key，用返回 Map 中【元素】对应的值作为缓存值
          // 注意此时【对象集合参数】需要在 Cacheable.key() 中，需要有 @CacheKey 注解
      }
  }
  ```

### 与其他注解搭配使用时的说明
* 与 Spring 的 `@CachePut` 搭配时，同样符合上面的例子。
* 与 `@CacheEvict` 搭配时，若注解的 `@CacheEvict.key()` 参数中没有 `#result`，对【方法】返回类型无要求；若 key 中有 `#result`，【方法】返回类型需要是 `Map` 或 `List`。
* 与 Spring 的 `@CachePut`、`@CacheEvict` 搭配，若 key 参数中已有 `#result`， 则可以没有【对象集合参数】的引用。
* 与 `@CacheRemove` 搭配时，对【方法】返回类型无要求。
* 与 JSR-107 的 `@CachePut` 搭配时，对【方法】返回类型无要求，可参照下面的示例：

###  JSR-107 的 @CachePut
* 单个参数做key，未配置 `@CacheKey`：
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(Integer fooId, @CacheValue String value) {
          // 用 fooId 参数生成缓存的 key，用 value 作为缓存值
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheValue Map  fooIdValueMap) {
          // 此时方法的 @CacheValue 参数必须为 Map 类型
          // 用 fooIdValueMap 中的每个 Entry 的 key 分别生成缓存的 key，用 Entry 的 value 作为缓存值
      }
  }
  ```

* 多个参数做key，未配置 `@CacheKey`：
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(Integer fooId, String arg1, @CacheValue String value) {
          // 用 fooId 和 arg1 两个参数生成缓存的 key，用 value 作为缓存值
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheValue Map fooIdValueMap, String arg1) {
          // 此时方法的 @CacheValue 参数必须为 Map 类型
          // 用 fooIdValueMap 中的每个 Entry 的 key 分别和 arg1 参数生成缓存的 key，用 Entry 的 value 作为缓存值
      }
  }
  ```

* 只有【对象参数】上有 `@CacheKey`：
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(@CacheKey Integer fooId, String arg1, @CacheValue String value) {
          // 用 fooId 参数生成缓存的 key，用 value 作为缓存值
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map fooIdValueMap, String arg1) {
          // 此时方法的 @CacheValue 参数必须为 Map 类型
          // 用 fooIdValueMap 中的每个 Entry 的 key 分别生成缓存的 key，用 Entry 的 value 作为缓存值
      }
  }
  ```

* 若干参数上有 `@CacheKey`：
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(@CacheKey Integer fooId, @CacheKey String arg1, String arg2, @CacheValue String value) {
          // 用 fooId 和 arg1 两个参数生成缓存的 key，用 value 作为缓存值
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map fooIdValueMap, @CacheKey String arg1, String arg2) {
        // 此时方法的 @CacheValue 参数必须为 Map 类型
        // 用 fooIdValueMap 中的每个 Entry 的 key 分别和 arg1 参数生成缓存的 key，用 Entry 的 value 作为缓存值
      }
  }
  ```

### 总结和补充
1. `@CacheAsMulti` 注解不能替代 Spring 缓存注解中的 key 参数，例如：`@Cacheable.key()`
   ，也不能替代 `@CacheKey`、`@CacheValue` 注解。
2. 如果使用自定义的 `KeyGenerator`，则会用【对象集合参数】的每个【元素】和其他参数组成 `Object[]`
   传入 `KeyGenerator.generate(Object, Method, Object...)` 计算缓存 key；自定义的 `CacheKeyGenerator` 也一样。
3. 与 `@Cacheable`、`@CacheResult`、`@CachePut` 注解搭配使用时，若 `CacheAsMulti.strictNull()` 为 `true`
   且方法的返回类型是 `Map`，【元素】在 `Map` 中对应的值为 `null` 就会缓存 `null`，【元素】在 `Map` 中不存在就不缓存。
4. 与 `@CachePut` 和 `@CacheEvict` 搭配，注解的 key 参数配置了 `#result` 时，若方法的返回类型是 `Map`，对于 `Map`
   中不存在的【元素】会使用 `null` 作为缺省值来计算缓存 key 和 condition、unless 条件。
5. `@Cacheable.condition()`、`@Cacheable.unless()` 等条件表达式是用【对象集合参数】中的每个【元素】分别计算，只将不符合的【元素】排除，而不是整个集合。

## 缓存接口及转换

### EnhancedCache 接口
`org.springframework.cache.Cache` 接口只定义了单个缓存操作，并不支持批量操作，为此定义了 `EnhancedCache` 接口扩充了 `multiGet`、`multiPut`、`multiEvict` 三个批量操作方法。

当使用某种缓存介质时，需要有对应的 `EnhancedCache` 接口实现。如果使用的介质没有对应的 `EnhancedCache` 实现，则会使用默认的 `EnhancedCacheConversionService.EnhancedCacheAdapter` 进行适配，会使用循环单个操作来实现批量操作，效率较低。同时在对象创建的时候会出现一条 warn 级别的日志。

### EnhancedCacheConverter\<T\> 接口
每种缓存介质还需要定义一个转换器用来将 `Cache` 自动转为 `EnhancedCache`，转换器实现的接口为 `EnhancedCacheConverter`。BeanFactory 中注册的转换器会自动加载到 `EnhancedCacheConversionService` 中用来将 Spring 原有的 `Cache` 转为 `EnhancedCache`。

### 默认实现
包里已经实现了 `RedisCache`、`ConcurrentMapCache`、`Ehcache`、`caffeineCache` 的 `EnhancedCache` 接口和相应的转换器，具体可到 `cache.convert.converter` 下查看。


## 工作原理
### 拦截器
1. 在标准的 BeanDefinition 之后，修改原有的 `OperationSource` 和 `Interceptor` 的 Bean 定义，使用自定义的（继承原有的）来替换。
2. 在原有 `OperationSource` 查询构建 `Operation` 后，查询构建 `MultiOperation` 并缓存。
3. 在原有 `Interceptor` 执行拦截前，查询是否缓存有对应的 `MultiOperation`，如果有则拦截执行。

### 批量缓存
1. 定义 `EnhancedCache` 扩充 Spring 的 `Cache`。
2. 定义 `EnhancedCacheConverter` 将 `Cache` 转为 `EnhancedCache`。
3. 对应 `Cache` 的实现类，增加 `EnhancedCache` 和 `EnhancedCacheConverter` 的实现类。
4. 定义 `EnhancedCacheConversionService` 将所有 `EnhancedCacheConverter` （包括使用者自定义的）自动注入进来。
5. 定义 `EnhancedCacheResolver` 包装 `CacheResolver`，并注入 `EnhancedCacheConversionService`，在调用 `resolveCaches` 获取 `Cache` 时将其转换为 `EnhancedCache`。

## 开发总结

### 用到的 Utils
- `GenericTypeResolver` 处理泛型类
- `ReflectionUtils` 反射工具类
- `ResolvableType` 处理各种字段类型、返回类型、参数类型
- `AnnotationUtils` 注解工具类，例如向上找父类的注解
- `AnnotatedElementUtils` 被注释的元素工具类，例如查找合并注解
- `MergedAnnotations` 合并注解的操作工具

### 小知识点
- Spring 的 `@AliasFor` 注解参数别名就是利用上述的 Spring 注解工具实现的。
- `Aware` 的处理需要显示的实现，例如在 `BeanPostProcessor` 的实现中。
- 如果一个 `Map` 没有对应的 `Set` 实现，可以用 `Collections.newSetFromMap(new ConcurrentHashMap<>(16))`。
- 处理 `@Autowried` 注解的是 `AutowiredAnnotationBeanPostProcessor`。
- 处理实现 `ApplicationContextAware` 接口的是 `ApplicationContextAwareProcessor`。
- java使用反射获取参数名得到的是arg0、arg1时，除了网上的解决办法（javac -parameters），还可以使用spring的 DefaultParameterNameDiscoverer。

## 扩展

### 缓存有效期

#### Spring 的缓存没有有效期？

针对不同的缓存缓存方式单独配置。例如Redis：

```yaml
spring:
  cache:
    redis:
      time-to-live: PT15M  #缓存15分钟
```

#### 灵活的TTL

> 前提：Spring 为缓存注解中每一个配置的 CacheName，都生成一个单独的 Cache 对象。

通常可以通过下面三种方式来实现：
* 自定义 `CacheManager` 或 `CacheResolver`。
* 使用其他缓存框架（不能再使用 `@CacheAsMulti`），例如 JetCache。
* 针对不同的缓存的扩展点单独定制。

#### 针对Redis的扩展点单独配置

只要实现 `RedisCacheManagerBuilderCustomizer` 接口，就可以在 `RedisCacheManager` 生成前，设置 `RedisCacheManagerBuilder` 实现自定义配置。

> 具体实现查看 `RedisCacheCustomizer` 类。

之后只需增加如下配置：
```yaml
spring:
  cache:
    redis:
      time-to-live: PT15M  #默认缓存15分钟
      cache-as-multi: #下面是 cache-as-multi 的配置
        serialize-to-json: true #使用 RedisSerializer.json() 序列化
        cache-name-time-to-live-map: #cacheName对应的缓存时间
          foo: PT15S  #foo缓存15秒
          demo: PT5M  #demo缓存5分钟
```







