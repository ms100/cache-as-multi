package io.github.ms100.cacheasmulti.cache.config;

import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import io.github.ms100.cacheasmulti.cache.convert.converter.EnhancedCacheConverter;
import io.github.ms100.cacheasmulti.jcache.config.EnhancedJCacheConfiguration;
import io.github.ms100.cacheasmulti.support.RedisCacheCustomizer;
import io.github.ms100.cacheasmulti.support.TypeMethodKeyGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import java.util.Collection;

/**
 * @author zhumengshuai
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ProxyCachingConfiguration.class)
public class EnhancedCacheAutoConfiguration extends AbstractCachingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public EnhancedCachingConfiguration enhancedCachingConfiguration() {
        return new EnhancedCachingConfiguration();
    }

    @Bean
    @ConditionalOnBean(type = "org.springframework.cache.jcache.config.ProxyJCacheConfiguration")
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public EnhancedJCacheConfiguration enhancedJCacheConfiguration() {
        return new EnhancedJCacheConfiguration();
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

    @Bean
    @ConditionalOnMissingBean
    public TypeMethodKeyGenerator typeMethodKeyGenerator() {
        return new TypeMethodKeyGenerator();
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.data.redis.cache.RedisCacheManager")
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.cache.redis.cache-as-multi")
    public RedisCacheCustomizer redisCacheCustomizer() {
        return new RedisCacheCustomizer();
    }
}
