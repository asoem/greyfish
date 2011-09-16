package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.GFComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 12:02
 */
public enum GreyfishExpressionFactory {
    INSTANCE;

    private EvaluatorFactory evaluatorFactory = new EvaluatorFactory() {
        @Override
        public Evaluator createEvaluator(String expression, VariableResolver resolver) throws SyntaxException {
            return new SeeEvaluator(expression, resolver);
        }
    };

    private GreyfishVariableResolverFactory variableResolverFactory = new GreyfishVariableResolverFactory() {

        private final CachedResolverConverter cachedConverter = new CachedResolverConverter(DefaultGreyfishResolverConverter.INSTANCE);

        @Override
        public <T extends GFComponent> GreyfishVariableResolver<T> create(Class<T> contextClass) {
            return new GreyfishVariableResolverConverterAdaptor<T>(cachedConverter, contextClass);
        }
    };

    public <T extends GFComponent> GreyfishExpression<T> create(String expression, Class<T> contextClass) throws SyntaxException {
        return new GreyfishExpression<T>(expression, evaluatorFactory, variableResolverFactory.create(contextClass));
    }

    public void setEvaluatorFactory(EvaluatorFactory evaluatorFactory) {
        this.evaluatorFactory = checkNotNull(evaluatorFactory);
    }

    public void setVariableResolverFactory(GreyfishVariableResolverFactory variableResolverFactory) {
        this.variableResolverFactory = checkNotNull(variableResolverFactory);
    }

    public static Builder compileExpression(String expression) {
        return new Builder(expression);
    }

    public static class Builder {
        private String expression;

        public Builder(String expression) {
            this.expression = checkNotNull(expression);
        }

        public <T extends GFComponent> GreyfishExpression<T> forContext(Class<T> contextClass) throws SyntaxException {
            return INSTANCE.create(expression, contextClass);
        }
    }
}
