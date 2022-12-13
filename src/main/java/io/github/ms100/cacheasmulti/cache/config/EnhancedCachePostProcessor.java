package io.github.ms100.cacheasmulti.cache.config;

import io.github.ms100.cacheasmulti.cache.jcache.config.EnhancedJCacheConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.cache.jcache.interceptor.JCacheInterceptor;
import org.springframework.cache.jcache.interceptor.JCacheOperationSource;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 在Bean定义完成之后，替换掉 cacheOperationSource 和 cacheInterceptor
 *
 * @author Zhumengshuai
 */
@Slf4j
public class EnhancedCachePostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final String PROXY_JCACHE_CONFIGURATION_CLASS =
            "org.springframework.cache.jcache.config.ProxyJCacheConfiguration";

    @Nullable
    protected BeanFactory beanFactory;

    private static final boolean JSR107_PRESENT;

    private static final boolean JCACHE_IMPL_PRESENT;

    static {
        if (log.isDebugEnabled()) {
            log.debug("Instantiating EnhancedCachePostProcessor, wish everything goes well");
        }
        ClassLoader classLoader = EnhancedCachePostProcessor.class.getClassLoader();
        JSR107_PRESENT = ClassUtils.isPresent("javax.cache.Cache", classLoader);
        JCACHE_IMPL_PRESENT = ClassUtils.isPresent(PROXY_JCACHE_CONFIGURATION_CLASS, classLoader);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerCacheRelatedBeanDefinition(registry, CacheMatchingBeanNames.CACHING);

        if (JSR107_PRESENT && JCACHE_IMPL_PRESENT) {
            // 注册工厂Bean的定义
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(EnhancedJCacheConfiguration.class);
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(CacheMatchingBeanNames.JCACHE_FACTORY_BEAN_NAME, builder.getBeanDefinition());

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
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JCacheOperationSource.class);
            builder.setFactoryMethodOnBean("enhancedCacheOperationSource", beanNames.factoryBeanName);
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(beanNames.operationSourceBeanName, builder.getBeanDefinition());
        }

        BeanDefinition cacheInterceptor = registry.getBeanDefinition(beanNames.interceptorBeanName);
        if (!beanNames.factoryBeanName.equals(cacheInterceptor.getFactoryBeanName())) {
            registry.removeBeanDefinition(beanNames.interceptorBeanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JCacheInterceptor.class);
            builder.setFactoryMethodOnBean("enhancedCacheInterceptor", beanNames.factoryBeanName);
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            builder.addConstructorArgReference(beanNames.operationSourceBeanName);
            registry.registerBeanDefinition(beanNames.interceptorBeanName, builder.getBeanDefinition());
        }
    }

}
