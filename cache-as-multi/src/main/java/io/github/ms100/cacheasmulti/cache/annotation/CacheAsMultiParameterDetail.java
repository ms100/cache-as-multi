package io.github.ms100.cacheasmulti.cache.annotation;

import lombok.Getter;
import lombok.ToString;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * {@link CacheAsMulti @CacheAsMulti}注解的参数的详细信息
 *
 * @author Zhumengshuai
 */
@ToString
public class CacheAsMultiParameterDetail {
    private static final ExpressionParser PARSER = new SpelExpressionParser(new SpelParserConfiguration(SpelCompilerMode.OFF, null));
    /**
     * {@link CacheAsMulti @CacheAsMulti}注解的参数类型
     */
    @Getter
    private final Class<?> rawType;

    /**
     * {@link CacheAsMulti @CacheAsMulti}注解的参数位置
     */
    @Getter
    private final int position;

    /**
     * 参数上的全部注解
     */
    private final Annotation[] annotations;

    /**
     * 严格的null模式
     */
    @Getter
    private final boolean strictNull;
    @Getter
    @Nullable
    private final Expression asElementFieldExpression;

    public CacheAsMultiParameterDetail(Method method, int position) {
        Parameter parameter = method.getParameters()[position];
        this.annotations = parameter.getAnnotations();
        this.rawType = parameter.getType();
        this.position = position;
        CacheAsMulti annotation = parameter.getAnnotation(CacheAsMulti.class);
        this.strictNull = annotation.strictNull();
        if (StringUtils.hasLength(annotation.asElementField())) {
            this.asElementFieldExpression = PARSER.parseExpression("#this." + annotation.asElementField());
        } else {
            this.asElementFieldExpression = null;
        }
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

}