package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:09
 */
public class GreyfishExpression<T extends GFComponent> {

    private final Evaluator evaluator;
    private final VariableResolver variableResolver;

    public GreyfishExpression(Evaluator evaluator, VariableResolver resolver) {
        this.evaluator = evaluator;
        this.variableResolver = resolver;
    }

    public double evaluateAsDouble(T context) throws EvaluationException {
        variableResolver.setContext(context);
        return evaluator.evaluateAsDouble();
    }

    public double evaluateAsDouble(T context, Object ... args) throws EvaluationException {
        variableResolver.setContext(context);
        // TODO: add argument parsing capability to VariableResolver
        // variableResolver.setArguments(args)
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

    public Class<T> getContextClass() {
        return (Class<T>) variableResolver.getContext().getClass(); // TODO: Parametrize VariableResolver
    }

    @Override
    public String toString() {
        return getExpression();
    }
}