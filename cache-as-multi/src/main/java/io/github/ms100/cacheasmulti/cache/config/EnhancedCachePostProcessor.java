package io.github.ms100.cacheasmulti.cache.config;

import io.github.ms100.cacheasmulti.jcache.config.EnhancedJCacheConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.lang.Nullable;

/**
 * 在Bean定义完成之后，替换掉 cacheOperationSource 和 cacheInterceptor
 *
 * @author Zhumengshuai
 */
@Slf4j
public class EnhancedCachePostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Nullable
    protected BeanFactory beanFactory;

    static {
        if (log.isDebugEnabled()) {
            log.debug("Instantiating EnhancedCachePostProcessor, wish everything goes well");
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerCacheRelatedBeanDefinition(registry, CacheMatchingBeanNames.CACHING);

        if (registry.containsBeanDefinition(CacheMatchingBeanNames.JCACHE.factoryBeanName)) {
            registerCacheRelatedBeanDefinition(registry, CacheMatchingBeanNames.JCACHE);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void registerCacheRelatedBeanDefinition(BeanDefinitionRegistry registry, CacheMatchingBeanNames beanNames) throws BeansException {
        BeanDefinition cacheOperationSource = registry.getBeanDefinition(beanNames.operationSourceBeanName);
        if (!beanNames.factoryBeanName.equals(cacheOperationSource.getFactoryBeanName())) {
            registry.removeBeanDefinition(beanNames.operationSourceBeanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
            builder.setFactoryMethodOnBean(beanNames.operationSourceBeanName, beanNames.factoryBeanName);
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(beanNames.operationSourceBeanName, builder.getBeanDefinition());
        }

        BeanDefinition cacheInterceptor = registry.getBeanDefinition(beanNames.interceptorBeanName);
        if (!beanNames.factoryBeanName.equals(cacheInterceptor.getFactoryBeanName())) {
            registry.removeBeanDefinition(beanNames.interceptorBeanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
            builder.setFactoryMethodOnBean(beanNames.interceptorBeanName, beanNames.factoryBeanName);
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            builder.addConstructorArgReference(beanNames.operationSourceBeanName);
            registry.registerBeanDefinition(beanNames.interceptorBeanName, builder.getBeanDefinition());
        }
    }

    @RequiredArgsConstructor
    enum CacheMatchingBeanNames {
        /**
         * Spring 缓存相关的bean
         */
        CACHING(
                "enhancedCachingConfiguration",
                "cacheOperationSource",
                "cacheInterceptor"
        ),
        /**
         * jsr-107 缓存相关的bean
         */
        JCACHE(
                "enhancedJCacheConfiguration",
                "jCacheOperationSource",
                "jCacheInterceptor"
        );

        public final String factoryBeanName;
        public final String operationSourceBeanName;
        public final String interceptorBeanName;
    }
}
