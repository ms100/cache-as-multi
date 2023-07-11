## More Examples of Cacheable and CacheResult

When a method has multiple parameters, here are some examples:

When a method has multiple parameters, the key generation for caching may vary based on the configuration
of `@Cacheable.key()` or the presence of `@CacheKey` annotations on the method parameters.

Here are some examples:

* When `@Cacheable.key()` is not configured or when `@CacheResult` is used and there is no `@CacheKey` on the method
  parameters:
  ```java
  class FooService {
      @Cacheable(cacheNames = "foo", key="")
      @CacheResult(cacheName = "foo")
      public Foo getFoo(Integer fooId, String arg1) {
          // generate cache key using fooId and arg1 parameters, use return value as cache value
      }
  
      @Cacheable(cacheNames = "foo", key="")
      @CacheResult(cacheName = "foo")
      public Map<Integer, Foo> getMultiFoo(@CacheAsMulti Collection<Integer> fooIds, String arg1) {
          // generate cache key for each element in fooIds parameter using arg1 parameter, use corresponding value from return Map as cache value
      }
  }
  ```

* When `@Cacheable.key()` only refers to the `@CacheKey` annotated parameter of an object parameter or when `@CacheKey`
  is used only on the object parameter:
  ```java
  class FooService {
      @Cacheable(cacheNames = "foo", key="#fooId")
      @CacheResult(cacheName = "foo")
      public Foo getFoo(@CacheKey Integer fooId, String arg1) {
          // generate cache key using fooId parameter, use return value as cache value
      }
  
      @Cacheable(cacheNames = "foo", key="#fooIds")
      @CacheResult(cacheName = "foo")
      public Map<Integer, Foo> getMultiFoo(@CacheAsMulti @CacheKey Collection<Integer> fooIds, String arg1) {
          // generate cache key for each element in fooIds parameter, use corresponding value from return Map as cache value
      }
  }
  ```

* When `@Cacheable.key()` refers to multiple parameters or when there are multiple `@CacheKey` annotations on the method
  parameters:
  ```java
  class FooService {
      @Cacheable(cacheNames = "foo", key="#fooId+#arg1")
      @CacheResult(cacheName = "foo")
      public Foo getFoo(@CacheKey Integer fooId, @CacheKey String arg1, Float arg2) {
          // generate cache key using fooId and arg1 parameters, use return value as cache value
      }
  
      @Cacheable(cacheNames = "foo", key="#fooIds+#arg1")
      @CacheResult(cacheName = "foo")
      public Map<Integer, Foo> getMultiFoo(@CacheAsMulti @CacheKey Collection<Integer> fooIds, @CacheKey String arg1, Float arg2) {
          // generate cache key for each element in fooIds parameter using arg1 parameter, use corresponding value from return Map as cache value
          // Note that the object collection parameter needs to have a @CacheKey annotation and needs to be included in Cacheable.key()
      }
  }
  ```

## More Examples of @CachePut in JSR-107

* Multiple parameters as key, without configuring `@CacheKey`:
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(Integer fooId, String arg1, @CacheValue String value) {
          // Generate the cache key using the fooId and arg1 parameters, and use value as the cache value
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheValue Map<Integer,String> fooIdValueMap, String arg1) {
          // In this case, the @CacheValue parameter of the method must be of type Map
          // Generate a cache key using each Entry key in fooIdValueMap along with the arg1 parameter, and use Entry value as the cache value
      }
  }
  ```

* Only the `@CacheKey` annotation is present on the object parameter:
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(@CacheKey Integer fooId, String arg1, @CacheValue String value) {
          // Generate the cache key using the fooId parameter, and use value as the cache value
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map<Integer,String> fooIdValueMap, String arg1) {
          // In this case, the @CacheValue parameter of the method must be of type Map
          // Generate a cache key using each Entry key in fooIdValueMap, and use Entry value as the cache value
      }
  }
  ```

* If there are `@CacheKey` annotations on multiple parameters:
  ```java
  class FooService {
      @CachePut(cacheName = "foo")
      public void putFoo(@CacheKey Integer fooId, @CacheKey String arg1, String arg2, @CacheValue String value) {
          // Generates a cache key using fooId and arg1 parameters, and uses value as the cache value
      }
  
      @CachePut(cacheName = "foo")
      public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map<Integer,String> fooIdValueMap, @CacheKey String arg1, String arg2) {
          // The @CacheValue parameter of this method must be of type Map
          // Generates a cache key using each key of the entries in fooIdValueMap and arg1 parameter, and uses the value of the entry as the cache value
      }
  }
  ```