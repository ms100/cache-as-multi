/*
 * Copyright 2002-2021 the original author or authors.
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

package io.github.ms100.cacheasmulti.cache.interceptor;

import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMulti;
import io.github.ms100.cacheasmulti.cache.annotation.CacheAsMultiParameterDetail;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.interceptor.CacheEvictOperation;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.CompoundExpression;
import org.springframework.expression.spel.ast.Indexer;
import org.springframework.expression.spel.ast.Literal;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;
import org.springframework.expression.spel.ast.VariableReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 被{@link  CacheAsMulti @CacheAsMulti}注解的方法进行解析的结果，
 * 缓存在{@link EnhancedCachingOperationSource}中
 *
 * @author zhumengshuai
 */
@Slf4j
class CacheAsMultiOperation<O extends CacheOperation> extends AbstractCacheAsMultiOperation {

    /**
     * java8 编译未配置-parameters参数时，获取到的参数名是arg0，所以用这个类
     */
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    @Getter
    protected final O operation;

    public CacheAsMultiOperation(Method method, O operation, CacheAsMultiParameterDetail parameterDetail) {
        super(method, parameterDetail);
        this.operation = operation;
        validateParameterDetail(method, operation, parameterDetail);

        if (returnTypeMaker == null && !(operation instanceof CacheEvictOperation)) {
            throw new IllegalStateException("The returnType must not be null when operation is not instanceof CacheEvictOperation on " + method);
        }
    }

    protected static void validateParameterDetail(
            Method method, CacheOperation operation, CacheAsMultiParameterDetail parameterDetail) {

        if (StringUtils.hasText(operation.getKey())) {
            KeyExpressionParser keyExpressionParser = new KeyExpressionParser(operation.getKey());

            String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
            Objects.requireNonNull(parameterNames);
            int parameterPosition = parameterDetail.getPosition();

            boolean parameterInExpression = keyExpressionParser.containParameter(parameterPosition)
                    || keyExpressionParser.containParameter(parameterNames[parameterPosition]);

            if (!parameterInExpression) {
                if (operation instanceof CachePutOperation || operation instanceof CacheEvictOperation) {
                    parameterInExpression = keyExpressionParser.containParameter("result");
                }
            }

            if (!parameterInExpression) {
                throw new IllegalStateException("The @CacheAsMulti parameter or result should be in key expression on " + method);
            }
        }
    }

    protected static class KeyExpressionParser {

        private final Set<Integer> indexSet = new HashSet<>();

        private final Set<String> nameSet = new HashSet<>();

        private static final SpelExpressionParser PARSER = new SpelExpressionParser();

        private static final Pattern PATTERN_ARG_INDEX = Pattern.compile("^#(?:a|p)(\\d+)$");

        public static final String ROOT_VARIABLE = "#root";

        public static final String ARGS_VARIABLE = "args";

        public KeyExpressionParser(String keyExpression) {
            SpelExpression expression = (SpelExpression) PARSER.parseExpression(keyExpression);
            SpelNode ast = expression.getAST();
            scanNode(ast);
        }

        public boolean containParameter(String name) {
            return nameSet.contains(name);
        }

        public boolean containParameter(int position) {
            return indexSet.contains(position);
        }

        private void scanNode(SpelNode node) {
            if (node instanceof CompoundExpression) {
                SpelNode child0 = node.getChild(0);
                if (!(child0 instanceof VariableReference)) {
                    return;
                }
                String child0NodeName = child0.toStringAST();
                if (!ROOT_VARIABLE.equals(child0NodeName)) {
                    scanNode(child0);
                    return;
                }
                SpelNode child1 = node.getChild(1);
                if (!(child1 instanceof PropertyOrFieldReference) || !ARGS_VARIABLE.equals(child1.toStringAST())) {
                    return;
                }
                SpelNode child2 = node.getChild(2);
                if (!(child2 instanceof Indexer)) {
                    return;
                }
                Literal indexNode = (Literal) child2.getChild(0);
                String originalValue = indexNode.getOriginalValue();
                Objects.requireNonNull(originalValue);
                indexSet.add(Integer.valueOf(originalValue));
            } else if (node instanceof VariableReference) {
                String nodeName = node.toStringAST();
                if (ROOT_VARIABLE.equals(nodeName)) {
                    return;
                }

                Matcher m = PATTERN_ARG_INDEX.matcher(nodeName);
                if (m.matches()) {
                    indexSet.add(Integer.valueOf(m.group(1)));
                } else {
                    nameSet.add(nodeName.substring(1));
                }
            } else {
                int childCount = node.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    scanNode(node.getChild(i));
                }
            }
        }
    }
}
