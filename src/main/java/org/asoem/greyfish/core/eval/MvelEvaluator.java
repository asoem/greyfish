package org.asoem.greyfish.core.eval;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 13:27
 */
public class MvelEvaluator implements Evaluator {

    private String expression;
    private Serializable compiledExpression;
    private @Nullable VariableResolverFactory factory;

    @Override
    public double evaluateAsDouble() throws EvaluationException {
        return Double.class.cast(MVEL.executeExpression(compiledExpression, factory));
    }

    @Override
    public boolean evaluateAsBoolean() throws EvaluationException {
        return Boolean.class.cast(MVEL.executeExpression(compiledExpression, factory));
    }

    @Override
    public void setExpression(String expression) throws SyntaxException {
        this.expression = expression;
        this.compiledExpression = MVEL.compileExpression(expression);
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setResolver(@Nullable VariableResolver resolver) {
        this.factory = new GreyfishMvelVariableResolverFactory(checkNotNull(resolver));
    }

    private static class MvelVariableResolverAdaptor extends ForwardingVariableResolver implements org.mvel2.integration.VariableResolver {

        private final String name;
        private final VariableResolver delegate;

        public MvelVariableResolverAdaptor(String name, VariableResolver delegate) {
            this.name = checkNotNull(name);
            this.delegate = checkNotNull(delegate);
        }

        @Override
        public VariableResolver delegate() {
            return delegate;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class getType() {
            return Object.class;
        }

        @Override
        public void setStaticType(Class aClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getFlags() {
            return 0;
        }

        @Override
        public Object getValue() {
            return resolve(getName());
        }

        @Override
        public void setValue(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private static class GreyfishMvelVariableResolverFactory implements VariableResolverFactory {

        private VariableResolverFactory nextFactory;
        private final VariableResolver delegate;

        public GreyfishMvelVariableResolverFactory(VariableResolver resolver) {
            this.delegate = checkNotNull(resolver);
        }

        @Override
        public org.mvel2.integration.VariableResolver createVariable(String s, Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.mvel2.integration.VariableResolver createIndexedVariable(int i, String s, Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.mvel2.integration.VariableResolver createVariable(String s, Object o, Class<?> aClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.mvel2.integration.VariableResolver createIndexedVariable(int i, String s, Object o, Class<?> aClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.mvel2.integration.VariableResolver setIndexedVariableResolver(int i, org.mvel2.integration.VariableResolver variableResolver) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VariableResolverFactory getNextFactory() {
            return this.nextFactory;
        }

        @Override
        public VariableResolverFactory setNextFactory(VariableResolverFactory variableResolverFactory) {
            return this.nextFactory = variableResolverFactory;
        }

        @Override
        public org.mvel2.integration.VariableResolver getVariableResolver(String s) {
            return new MvelVariableResolverAdaptor(s, delegate);
        }

        @Override
        public org.mvel2.integration.VariableResolver getIndexedVariableResolver(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTarget(String s) {
            return isResolveable(s);
        }

        @Override
        public boolean isResolveable(String s) {
            try {
                delegate.canResolve(s);
                return true;
            } catch (VariableResolutionException e) {
                return false;
            }

        }

        @Override
        public Set<String> getKnownVariables() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int variableIndexOf(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isIndexedFactory() {
            return false;
        }
    }
}
