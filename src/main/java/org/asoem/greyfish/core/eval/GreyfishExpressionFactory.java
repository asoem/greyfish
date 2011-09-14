package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 12:02
 */
public enum GreyfishExpressionFactory {
    INSTANCE;

    private EvaluatorFactory evaluatorFactory = new EvaluatorFactory() {
        @Override
        public Evaluator createEvaluator(String expression) {
            return new SeeEvaluator(expression);
        }
    };

    private VariableResolverFactory variableResolverFactory = new VariableResolverFactory(DefaultGreyfishResolverConverter.INSTANCE);

    public <T extends GFComponent> GreyfishExpression<T> create(String expression, Class<? extends T> contextClass) {
        return new GreyfishExpression<T>(
                evaluatorFactory.createEvaluator(expression),
                variableResolverFactory.createForContext(contextClass));
    }

    public void setEvaluatorFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = evaluatorFactory;
    }

    public void setVariableResolverFactory(VariableResolverFactory variableResolverFactory) {
        this.variableResolverFactory = variableResolverFactory;
    }

    public static Builder compileExpression(String expression) {
        return new Builder(expression);
    }

    public static class Builder {
        private String expression;

        public Builder(String expression) {
            this.expression = expression;
        }

        public <T extends GFComponent> GreyfishExpression<T> forContext(Class<T> contextClass) {
            return INSTANCE.create(expression, contextClass);
        }
    }
}
