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
@ConditionalOnBean(ProxyCachingConfiguration.class)
@Configuration(proxyBeanMethods = false, value = CacheMatchingBeanNames.CACHING_FACTORY_BEAN_NAME)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class EnhancedCachingConfiguration extends AbstractCachingConfiguration {

    /**
     * 通过{@link EnhancedCachePostProcessor}注册
     */
    public CacheOperationSource enhancedCacheOperationSource() {
        return new EnhancedCachingOperationSource();
    }

    /**
     * 通过{@link EnhancedCachePostProcessor}注册
     */
    public CacheInterceptor enhancedCacheInterceptor(CacheOperationSource cacheOperationSource) {
        CacheInterceptor interceptor = new EnhancedCachingInterceptor();
        interceptor.configure(this.errorHandler, this.keyGenerator, this.cacheResolver, this.cacheManager);
        interceptor.setCacheOperationSource(cacheOperationSource);
        return interceptor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public static EnhancedCachePostProcessor enhancedCachingPostProcessor() {
        return new EnhancedCachePostProcessor();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public EnhancedCacheConversionService enhancedCacheConversionService(Collection<EnhancedCacheConverter<?>> converters) {
        return new EnhancedCacheConversionService(converters);
    }

}
