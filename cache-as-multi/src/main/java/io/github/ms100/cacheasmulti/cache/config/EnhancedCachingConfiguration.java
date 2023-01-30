package io.github.ms100.cacheasmulti.cache.config;

import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import io.github.ms100.cacheasmulti.cache.convert.converter.EnhancedCacheConverter;
import io.github.ms100.cacheasmulti.cache.interceptor.EnhancedCachingInterceptor;
import io.github.ms100.cacheasmulti.cache.interceptor.EnhancedCachingOperationSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import java.util.Collection;

/**
 * @author zhumengshuai
 */
public class EnhancedCachingConfiguration extends AbstractCachingConfiguration {

    // 通过{@link EnhancedCachePostProcessor}注册
    public CacheOperationSource cacheOperationSource() {
        return new EnhancedCachingOperationSource();
    }

    // 通过{@link EnhancedCachePostProcessor}注册
    public CacheInterceptor cacheInterceptor(CacheOperationSource cacheOperationSource) {
        CacheInterceptor interceptor = new EnhancedCachingInterceptor();
        interceptor.configure(this.errorHandler, this.keyGenerator, this.cacheResolver, this.cacheManager);
        interceptor.setCacheOperationSource(cacheOperationSource);
        return interceptor;
    }

}
