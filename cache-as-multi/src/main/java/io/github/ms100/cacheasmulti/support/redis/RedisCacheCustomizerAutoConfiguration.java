package io.github.ms100.cacheasmulti.support.redis;

import io.github.ms100.cacheasmulti.cache.config.EnhancedCacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhumengshuai
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.data.redis.cache.RedisCacheManager")
@ConditionalOnBean(EnhancedCacheAutoConfiguration.class)
public class RedisCacheCustomizerAutoConfiguration extends AbstractCachingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.cache.redis.cache-as-multi", name = "serialize-to-json", havingValue = "true")
    public RedisCacheSerializeCustomizer redisCacheSerializeCustomizer() {
        return new RedisCacheSerializeCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.cache.redis.cache-as-multi")
    public RedisCacheTtlCustomizer redisCacheTtlCustomizer() {
        return new RedisCacheTtlCustomizer();
    }
}
