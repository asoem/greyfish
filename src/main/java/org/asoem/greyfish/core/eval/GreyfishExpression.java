package org.asoem.greyfish.core.eval;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.individual.AgentComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:09
 */
public class GreyfishExpression<T extends AgentComponent> {

    private final Evaluator evaluator;
    private final GreyfishVariableResolver<T> variableResolver;

    public GreyfishExpression(String expression, EvaluatorFactory evaluatorFactory, GreyfishVariableResolver<T> resolver) {
        this.variableResolver = checkNotNull(resolver);
        this.evaluator = checkNotNull(evaluatorFactory).createEvaluator(expression, this.variableResolver);
    }

    public double evaluateAsDouble(T context) throws EvaluationException {
        variableResolver.setContext(context);
        return evaluator.evaluateAsDouble();
    }

    public double evaluateAsDouble(T context, String n1, Object v1) throws EvaluationException {
        variableResolver.setContext(context);
        variableResolver.setNext(VariableResolvers.forMap(ImmutableMap.of(n1, v1)));
        return evaluator.evaluateAsDouble();
    }

    public boolean evaluateAsBoolean(T context) throws EvaluationException {
        variableResolver.setContext(context);
        return evaluator.evaluateAsBoolean();
    }

    public void setExpression(String expression) {
        evaluator.setExpression(expression);
    }

    public String getExpression() {
        return evaluator.getExpression();
    }

    @SuppressWarnings("unchecked")
    public Class<T> getContextClass() {
        return (Class<T>) variableResolver.getContext().getClass();
    }

    @Override
    public String toString() {
        return getExpression();
    }
}