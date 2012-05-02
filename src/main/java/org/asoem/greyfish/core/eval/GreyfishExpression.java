package org.asoem.greyfish.core.eval;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:09
 */
public class GreyfishExpression implements Expression {

    private static final Pattern DOLLAR_FUNCTION_PATTERN = Pattern.compile("\\$\\(([^\\)]+)\\)");
    private static final Logger LOGGER = LoggerFactory.getLogger(GreyfishExpression.class);

    private final Evaluator evaluator;
    private final String expression;

    @Inject
    public GreyfishExpression(@Assisted String expression, Evaluator evaluator) {
        this.evaluator = checkNotNull(evaluator);
        this.expression = checkNotNull(expression);
        this.evaluator.setExpression(parameterizeDollarFunction(expression));
    }

    public EvaluationResult evaluateForContext(Object context) throws EvaluationException {
        checkNotNull(context, "Context must not be null");
        return evaluate(createContextResolver(context));
    }

    public EvaluationResult evaluateForContext(Object context, String n1, Object v1) throws EvaluationException {
        checkNotNull(context, "Context must not be null");
        final VariableResolver contextResolver = createContextResolver(context);
        final VariableResolver resolver = VariableResolvers.forMap(ImmutableMap.of(n1, v1));
        contextResolver.append(resolver);
        return evaluate(contextResolver);
    }

    public EvaluationResult evaluateForContext(Object context, Map<String, ?> localVariables) throws EvaluationException {
        checkNotNull(context, "Context must not be null");
        final VariableResolver contextResolver = createContextResolver(context);
        final VariableResolver resolver = VariableResolvers.forMap(localVariables);
        contextResolver.append(resolver);
        return evaluate(contextResolver);
    }

    @Override
    public EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException {
        evaluator.setResolver(resolver);
        final EvaluationResult result = evaluator.evaluate();
        LOGGER.debug("{} got evaluated to {} with resolver {}", expression, result, resolver);
        return result;
    }

    @Override
    public EvaluationResult evaluate() throws EvaluationException {
        return evaluator.evaluate();
    }

    private static String parameterizeDollarFunction(String expression) {
        return DOLLAR_FUNCTION_PATTERN.matcher(expression).replaceAll("\\$($1, _ctx_)");
    }

    @Override
    public String getExpression() {
        return expression;
    }

    public static VariableResolver createContextResolver(@Nullable Object ctx) {
        return VariableResolvers.forMap(ImmutableMap.of("_ctx_", ctx));
    }

    @Override
    public String toString() {
        return "GreyfishExpression{" +
                "evaluator=" + evaluator +
                ", expression='" + expression + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GreyfishExpression that = (GreyfishExpression) o;

        return evaluator.equals(that.evaluator) && expression.equals(that.expression);

    }

    @Override
    public int hashCode() {
        int result = evaluator.hashCode();
        result = 31 * result + expression.hashCode();
        return result;
    }

    @Override
    public Evaluator getEvaluator() {
        return evaluator;
    }
}