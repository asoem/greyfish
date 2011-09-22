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
    private VariableResolverFactory factory = new GreyfishMvelVariableResolverFactory();
    private @Nullable VariableResolver variableResolver;

    @Override
    public double evaluateAsDouble() throws EvaluationException {

        return MVEL.executeExpression(compiledExpression, factory, Double.class);
    }

    @Override
    public boolean evaluateAsBoolean() throws EvaluationException {
        return MVEL.executeExpression(compiledExpression, factory, Boolean.class);
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

    public void setVariableResolver(@Nullable VariableResolver resolver) {
        this.variableResolver = checkNotNull(resolver);
    }

    private class MvelVariableResolverAdaptor implements VariableResolver, org.mvel2.integration.VariableResolver {

        private final String name;

        public MvelVariableResolverAdaptor(String name) {
            this.name = name;
        }

        @Override
        public Object resolve(String varName) throws VariableResolutionException {
            if (variableResolver == null)
                return null;
            return variableResolver.resolve(varName);
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

    private class GreyfishMvelVariableResolverFactory implements VariableResolverFactory {

        private VariableResolverFactory nextFactory;

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
            return new MvelVariableResolverAdaptor(s);
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
                variableResolver.resolve(s);
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
