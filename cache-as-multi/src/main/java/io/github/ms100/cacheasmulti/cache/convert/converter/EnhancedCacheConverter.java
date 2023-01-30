package io.github.ms100.cacheasmulti.cache.convert.converter;

import io.github.ms100.cacheasmulti.cache.EnhancedCache;
import io.github.ms100.cacheasmulti.cache.convert.EnhancedCacheConversionService;
import org.springframework.cache.Cache;
import org.springframework.core.convert.converter.Converter;

/**
 * {@link Cache} 到 {@link EnhancedCache} 的转换器
 * 会被自动加载到 {@link EnhancedCacheConversionService} 中
 *
 * @author Zhumengshuai
 */
public interface EnhancedCacheConverter<T> extends Converter<T, EnhancedCache> {

}
