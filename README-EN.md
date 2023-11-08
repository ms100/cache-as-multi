# CacheAsMulti

[中文](./README.md)

## Installation

### Maven

```xml

<dependency>
    <groupId>io.github.ms100</groupId>
    <artifactId>cache-as-multi</artifactId>
    <version>1.3.1</version>
</dependency>
```

## Recent Updates

### v1.3

If the List returned by a batch method cannot guarantee the same size and order as the "object collection parameter",
you can use `@CacheAsMulti.asElementField` to specify the field in the List where the elements of the "object collection
parameter" are located. This will be more suitable for database queries.

```java
class CarService {
    @Cacheable(cacheNames = "car")
    @CacheResult(cacheName = "car")
    public List<CarPO> findCars(@CacheAsMulti(asElementField = "info.id") List<Integer> ids) {
        // The size of the returned List does not have to be the same as ids.
    }

    public static class CarPO {
        private CarInfoPO info;
        private String name;
    }

    public static class CarInfoPO {
        private Integer id;
    }
}
```

## Usage

This annotation needs to be used in conjunction with the following two sets of annotations to achieve batch caching
operations on the method where the annotated parameter is located.

* Spring's caching annotations `@Cacheable`, `@CachePut`, `@CacheEvict`

* JSR-107 annotations `@CacheResult`, `@CachePut`, `@CacheRemove`, `@CacheKey`

> Only PROXY mode is supported, not ASPECTJ mode.

### @Cacheable and @CacheResult

#### Regular Method

Suppose there is a method for obtaining a single object, as follows:

```java
class FooService {
  public Foo getFoo(Integer fooId) {
    //...
  }
}

```

At this point, if you need to get a batch of objects, there are usually two ways to write it:

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

There are two changes to the method of obtaining batch objects compared to the method of obtaining single objects:

1. The input parameter changes from a single object (referred to as an "object parameter" below) to an object
   collection (referred to as an "object collection parameter" below), for example, `Integer` changes
   to `Collection<Integer>` or `Set<Integer>` or `List<Integer>`.
2. The return value changes from a single object to `Map<K, V>` or `List<V>`. For example, `Map<Integer, Foo>`
   or `List<Foo>`. If the returned type is `List`, it should be the same size as the "object collection parameter" and
   in the same order(PS: After version v1.3, this limitation no longer exists. Please refer to
   the [update details](#v13) for more information.).

#### Add Cache

In the above example, if you need to cache the method of obtaining a single object, you will use the `@Cacheable`
or `@CacheResult` annotation: (PS: Here, `@CacheResult` and `@Cacheable` are used together as an example, in actual use,
usually only one of them is used)

```java
class FooService {
  @Cacheable(cacheNames = "foo")
  @CacheResult(cacheName = "foo")
  public Foo getFoo(Integer fooId) {
    // Use fooId to generate cache key and calculate condition and unless conditions, use Foo as cache value
  }
}
```

If `@Cacheable` or `@CacheResult` is directly added to the method of getting batch objects, a cache key will be
generated for the entire【collection parameter】 and the returned `Map` or `List` will be used as a cache value.

However, we usually hope that it can be transformed into multiple `fooId => Foo` caches, that is: each【element】in
the【collection parameter】and its corresponding value are cached separately. *At this time, just add the @CacheAsMulti
annotation on the【collection parameter】to achieve the caching method we want.*

```java
class FooService {
  @Cacheable(cacheNames = "foo")
  @CacheResult(cacheName = "foo")
  public Map<Integer, Foo> getMultiFoo(@CacheAsMulti Collection<Integer> fooIds) {
    // Generate a cache key and calculate condition and unless conditions for each element in the fooIds collection, 
    // use the corresponding value in the Map as the cache value
  }

  @Cacheable(cacheNames = "foo")
  @CacheResult(cacheName = "foo")
  public List<Foo> getMultiFoo(@CacheAsMulti List<Integer> fooIds) {
    // Generate a cache key and calculate condition and unless conditions for each element in the fooIds collection, 
    // use the corresponding value in the List as the cache value
    // In the following examples, the handling method for returning List and returning Map is the same, 
    // so they will not be separately demonstrated.
  }
}
```

[More examples](./MORE-EXAMPLES-EN.md#more-examples-of-cacheable-and-cacheresult)

### Usage with other annotations

* When used with Spring's `@CachePut`, it follows the same example as above.
* When used with `@CacheEvict`, if the `@CacheEvict.key()` parameter in the annotation does not contain `#result`, there
  is no requirement for the return type of the method; if `#result` is present in the key, the return type of the method
  needs to be `Map` or `List`.
* When used with both Spring's `@CachePut` and `@CacheEvict`, if the key parameter already contains `#result`, there is
  no need for a reference to the object collection parameter.
* When used with `@CacheRemove`, there is no requirement for the return type of the method.
* When used with JSR-107's `@CachePut`, there is no requirement for the return type of the method, and the following
  example can be referred to:

### JSR-107's @CachePut

Single parameter as key, without configuring `@CacheKey`:

```java
class FooService {
    @CachePut(cacheName = "foo")
    public void putFoo(Integer fooId, @CacheValue String value) {
        // Generate the cache key using the fooId parameter and use value as the cache value
    }

    @CachePut(cacheName = "foo")
    public void putMultiFoo(@CacheAsMulti @CacheValue Map<Integer, String> fooIdValueMap) {
        // In this case, the @CacheValue parameter of the method must be of type Map
        // Generate a cache key using each Entry key in fooIdValueMap, and use Entry value as the cache value
    }
}
```

[More examples](./MORE-EXAMPLES-EN.md#more-examples-of-cacheput-in-jsr-107)

### Summary and Supplement

1. `@CacheAsMulti` annotation cannot replace the `key` parameter in Spring cache annotations, such
   as `@Cacheable.key()`,
   nor the `@CacheKey` and `@CacheValue` annotations.
2. If a custom `KeyGenerator` is used, an `Object[]` will be generated by combining each `element` of
   the `collection parameter`
   and other parameters to calculate the cache key using `KeyGenerator.generate(Object, Method, Object...)`; the same
   goes for custom `CacheKeyGenerator`.
3. When used in conjunction with `@Cacheable`,`@CacheResult` and `@CachePut`, if `CacheAsMulti.strictNull()`
   is `true` and the return type of the method is `Map`, the value of the corresponding `element` in the `Map`
   is `null`, then `null` will be cached, and if the `element` does not exist in the `Map`, it will not be cached.
4. When used in conjunction with `@CachePut` and `@CacheEvict`, if the key parameter of the annotation is configured
   with `#result`,
   and the return type of the method is `Map`, `null` will be used as the default value to calculate the cache key and
   condition and unless conditions for the `element`
   that does not exist in the `Map`.
5. `@Cacheable.condition()`, `@Cacheable.unless()` and other conditional expressions are calculated using each `element`
   of the `collection parameter`,
   and only exclude the `elements` that do not meet the conditions, rather than the entire collection.

## Cache Interface and Conversion

### EnhancedCache Interface

The `org.springframework.cache.Cache` interface only defines a single cache operation and does not support batch
operations. Therefore, the `EnhancedCache` interface is defined to extend three batch operation
methods: `multiGet`, `multiPut`, and `multiEvict`.

When using a certain caching medium, there needs to be a corresponding implementation of the `EnhancedCache` interface.
If the medium used does not have a corresponding implementation of `EnhancedCache`, the
default `EnhancedCacheConversionService.EnhancedCacheAdapter` will be used for adaptation, which implements batch
operations by iterating through single operations, resulting in lower efficiency. At the same time, there will be a
warn-level log when the object is created.

### EnhancedCacheConverter\<T\> Interface

Each caching medium also needs to define a converter to automatically convert `Cache` to `EnhancedCache`. The interface
implemented by the converter is `EnhancedCacheConverter`. The converters registered in the `BeanFactory` will
automatically be loaded into the `EnhancedCacheConversionService` to convert Spring's original `Cache`
to `EnhancedCache`.

### Default Implementation

The `EnhancedCache` interface and the corresponding converters for `RedisCache`, `ConcurrentMapCache`, `Ehcache`,
and `caffeineCache` have been implemented in the package, which can be viewed under `cache.convert.converter`.

## Working Principle

### Interceptor

1. After the standard `BeanDefinition`, modify the original `OperationSource` and `Interceptor` Bean definitions, and
   replace them with custom (inherited original) definitions.
2. After the original `OperationSource` queries and constructs an `Operation`, query and construct a `MultiOperation`
   and cache it.
3. Before the original `Interceptor` executes the interception, check whether the corresponding `MultiOperation` is
   cached. If it is, then intercept and execute.

### Batch Caching

1. Define `EnhancedCache` to extend Spring's `Cache`.
2. Define `EnhancedCacheConverter` to convert `Cache` to `EnhancedCache`.
3. Implement `EnhancedCache` and `EnhancedCacheConverter` in the corresponding implementation class of `Cache`.
4. Define `EnhancedCacheConversionService` to automatically inject all `EnhancedCacheConverter` (including those defined
   by the user).
5. Define `EnhancedCacheResolver` to wrap `CacheResolver`, inject `EnhancedCacheConversionService`, and convert `Cache`
   to `EnhancedCache` when calling `resolveCaches` to obtain `Cache`.

## Development Summary

### Utils Used

- `GenericTypeResolver` handles generic classes.
- `ReflectionUtils` is a reflection utility class.
- `ResolvableType` handles various field types, return types, and parameter types.
- `AnnotationUtils` is an annotation utility class, for example, to find the annotation of the parent class.
- `AnnotatedElementUtils` is a utility class for annotated elements, for example, to find merged annotations.
- `MergedAnnotations` is an operation utility for merged annotations.

### Small Points

- The parameter alias of Spring's `@AliasFor` annotation is implemented using the above Spring annotation tools.
- Handling of `Aware` requires explicit implementation, such as in the implementation of `BeanPostProcessor`.
- If a `Map` has no corresponding `Set` implementation, you can
  use `Collections.newSetFromMap(new ConcurrentHashMap<>(16))`.
- `AutowiredAnnotationBeanPostProcessor` handles the `@Autowired` annotation.
- `ApplicationContextAwareProcessor` handles the implementation of the `ApplicationContextAware` interface.
- When using reflection in Java to get parameter names, if the names are arg0, arg1, etc., in addition to the solution
  found online (using javac -parameters), you can also use Spring's `DefaultParameterNameDiscoverer`.

## Extension

### Cache Expiration

#### Does Spring Cache support cache expiration?

Cache expiration can be configured separately for different caches and different cache implementations, for example
Redis:

```yaml
spring:
  cache:
    redis:
      time-to-live: PT15M  #cache for 15 minutes
```

#### Flexible TTL

> Premise: Spring generates a separate Cache object for each CacheName configured in the cache annotation.

Usually, you can achieve this in the following three ways:

* Customize `CacheManager` or `CacheResolver`.
* Use other caching frameworks (cannot use `@CacheAsMulti`), such as JetCache.
* Customize separately for different caching implementation.

#### Separate configuration for Redis extension points

Simply implement the `RedisCacheManagerBuilderCustomizer` interface to customize configuration before
the `RedisCacheManager`
is generated.

> See `RedisCacheCustomizer` class for details.

After that, just add the following configuration:

```yaml
spring:
  cache:
    redis:
      time-to-live: PT15M  #default cache for 15 minutes
      cache-as-multi: #Below are the cache-as-multi configurations
        serialize-to-json: true #Use RedisSerializer.json() for serialization
        cache-name-time-to-live-map: #Cache time corresponding to cacheName
          foo: PT15S  #foo cache for 15 seconds
          demo: PT5M  #demo cache for 5 minutes
```



