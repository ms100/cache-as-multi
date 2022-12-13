/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ms100.cacheasmulti.jcache.interceptor;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.interceptor.EnhancedCacheResolver;
import io.github.ms100.cacheasmulti.jcache.EnhancedJCacheCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import javax.cache.Cache;
import javax.cache.annotation.CacheInvocationContext;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 把JSR-107的{@link javax.cache.annotation.CacheResolver}包装成Spring的{@link CacheResolver}
 * 将原本返回的JSR-107的{@link Cache}转成{@link EnhancedCache}
 *
 * @author zhumengshuai
 * @see org.springframework.cache.jcache.interceptor.CacheResolverAdapter
 */
@RequiredArgsConstructor
class EnhancedJCacheResolverAdapter implements EnhancedCacheResolver {

	private final ConcurrentMap<Cache<Object, Object>, EnhancedCache> enhancedCacheCache = new ConcurrentHashMap<>(1024);

	private final javax.cache.annotation.CacheResolver cacheResolver;

	@Override
	public Collection<? extends EnhancedCache> resolveCaches(CacheOperationInvocationContext<?> context) {
		if (!(context instanceof CacheInvocationContext<?>)) {
			throw new IllegalStateException("Unexpected context " + context);
		}

		CacheInvocationContext<?> cacheInvocationContext = (CacheInvocationContext<?>) context;
		Cache<Object, Object> cache = cacheResolver.resolveCache(cacheInvocationContext);
		if (cache == null) {
			throw new IllegalStateException("Could not resolve cache for " + context + " using " + this.cacheResolver);
		}
		return Collections.singleton(convert(cache));
	}

	private EnhancedCache convert(Cache<Object, Object> cache) {
		return enhancedCacheCache.computeIfAbsent(cache, EnhancedJCacheCache::new);
	}

}
