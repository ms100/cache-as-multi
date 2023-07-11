## Cacheable 和 CacheResult 的更多示例

当方法有多个参数时，缓存的 key 可能因 `@Cacheable.key` 的配置或方法参数上的`@CacheKey` 注释而异。

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

* 使用 `@Cacheable` 时 `@Cacheable.key()` 只配置了【对象参数】的引用【或】使用 `@CacheResult`
  时只有【对象参数】上有 `@CacheKey`
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

## JSR-107 的 @CachePut 的更多示例

* 多个参数做key，未配置 `@CacheKey`：
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(Integer fooId, String arg1, @CacheValue String value) {
          // 用 fooId 和 arg1 两个参数生成缓存的 key，用 value 作为缓存值
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheValue Map<Integer,String> fooIdValueMap, String arg1) {
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
      public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map<Integer,String> fooIdValueMap, String arg1) {
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
      public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map<Integer,String> fooIdValueMap, @CacheKey String arg1, String arg2) {
        // 此时方法的 @CacheValue 参数必须为 Map 类型
        // 用 fooIdValueMap 中的每个 Entry 的 key 分别和 arg1 参数生成缓存的 key，用 Entry 的 value 作为缓存值
      }
  }
  ```
