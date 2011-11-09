package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 12:02
 */
public enum SingletonGreyfishExpressionFactory implements GreyfishExpressionFactory {
    INSTANCE;

    private EvaluatorFactory evaluatorFactory = new EvaluatorFactory() {
        @Override
        public Evaluator createEvaluator(String expression, VariableResolver resolver) throws SyntaxException {
            return new MvelEvaluator(expression, resolver);
        }
    };

    private GreyfishVariableResolverFactory variableResolverFactory = new GreyfishVariableResolverFactory() {
        private final CachedGreyfishVariableAccessorFactory cachedConverter = new CachedGreyfishVariableAccessorFactory(SingletonGreyfishGreyfishVariableAccessorFactory.INSTANCE);

        @Override
        public <T extends AgentComponent> GreyfishVariableResolver<T> create(Class<T> contextClass) {
            return new GreyfishVariableAccessorFactoryAdaptor<T>(cachedConverter, contextClass);
        }
    };

    @Override
    public <T extends AgentComponent> GreyfishExpression<T> create(String expression, Class<T> contextClass) throws SyntaxException {
        GreyfishVariableResolver<T> resolver = variableResolverFactory.create(contextClass);
        Evaluator evaluator = evaluatorFactory.createEvaluator(expression, resolver);
        return new GreyfishExpression<T>(expression, evaluator, resolver);
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

        public <T extends AgentComponent> GreyfishExpression<T> forContext(Class<T> contextClass) throws SyntaxException {
            return INSTANCE.create(expression, contextClass);
        }
    }
}
