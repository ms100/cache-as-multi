package io.github.ms100.cacheasmulti.jcache.config;

import io.github.ms100.cacheasmulti.cache.config.EnhancedCachePostProcessor;
import io.github.ms100.cacheasmulti.jcache.interceptor.EnhancedJCacheInterceptor;
import io.github.ms100.cacheasmulti.jcache.interceptor.EnhancedJCacheOperationSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.jcache.config.AbstractJCacheConfiguration;
import org.springframework.cache.jcache.config.ProxyJCacheConfiguration;
import org.springframework.cache.jcache.interceptor.JCacheInterceptor;
import org.springframework.cache.jcache.interceptor.JCacheOperationSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 定义新的Bean用来替换{@link ProxyJCacheConfiguration}中定义的
 * 通过{@link EnhancedCachePostProcessor}根据条件注册
 *
 * @author zhumengshuai
 */
public class EnhancedJCacheConfiguration extends AbstractJCacheConfiguration {

    // 通过{@link EnhancedCachePostProcessor}根据条件注册
    public JCacheOperationSource jCacheOperationSource() {
        return new EnhancedJCacheOperationSource(
                cacheManager, cacheResolver, exceptionCacheResolver, keyGenerator);
    }

    // 通过{@link EnhancedCachePostProcessor}根据条件注册
    public JCacheInterceptor jCacheInterceptor(JCacheOperationSource jCacheOperationSource) {
        JCacheInterceptor interceptor = new EnhancedJCacheInterceptor(errorHandler);
        interceptor.setCacheOperationSource(jCacheOperationSource);
        return interceptor;
    }

}
