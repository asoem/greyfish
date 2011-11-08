package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.utils.math.RandomUtils;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
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
    private @Nullable GreyfishMvelVariableResolverFactory factory;

    private Serializable compiledExpression;

    private static final ParserContext PARSER_CONTEXT = new ParserContext();
    static {
        PARSER_CONTEXT.addImport("max", MVEL.getStaticMethod(Math.class, "max", new Class[] {double.class, double.class}));
        PARSER_CONTEXT.addImport("log", MVEL.getStaticMethod(Math.class, "log", new Class[] {double.class}));
        PARSER_CONTEXT.addImport("gaussian", MVEL.getStaticMethod(RandomUtils.class, "gaussian", new Class[] {double.class, double.class}));
        PARSER_CONTEXT.addImport("poisson", MVEL.getStaticMethod(RandomUtils.class, "poisson", new Class[] {double.class}));
    }

    public MvelEvaluator() {
    }

    public MvelEvaluator(String expression) {
        setExpression(expression);
    }

    public MvelEvaluator(String expression, VariableResolver resolver) {
        setExpression(expression);
        setResolver(resolver);
    }

    @Override
    public double evaluateAsDouble() throws EvaluationException {
        return Number.class.cast(MVEL.executeExpression(compiledExpression, factory)).doubleValue();
    }

    @Override
    public boolean evaluateAsBoolean() throws EvaluationException {
        return Boolean.class.cast(MVEL.executeExpression(compiledExpression, factory));
    }

    @Override
    public void setExpression(String expression) throws SyntaxException {
        this.expression = expression;
        this.compiledExpression = MVEL.compileExpression(expression, PARSER_CONTEXT);
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setResolver(@Nullable VariableResolver resolver) {
        this.factory = new GreyfishMvelVariableResolverFactory(checkNotNull(resolver));
    }

    @Override
    public VariableResolver getResolver() {
        return factory == null ? null : factory.getVariableResolver();
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
        private final VariableResolver variableResolver;

        public GreyfishMvelVariableResolverFactory(VariableResolver resolver) {
            this.variableResolver = checkNotNull(resolver);
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

        public VariableResolver getVariableResolver() {
            return variableResolver;
        }

        @Override
        public org.mvel2.integration.VariableResolver getVariableResolver(String s) {
            return new MvelVariableResolverAdaptor(s, variableResolver);
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
                variableResolver.canResolve(s);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GreyfishMvelVariableResolverFactory that = (GreyfishMvelVariableResolverFactory) o;

            return !(nextFactory != null ? !nextFactory.equals(that.nextFactory) : that.nextFactory != null)
                    && variableResolver.equals(that.variableResolver);

        }

        @Override
        public int hashCode() {
            int result = nextFactory != null ? nextFactory.hashCode() : 0;
            result = 31 * result + variableResolver.hashCode();
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MvelEvaluator that = (MvelEvaluator) o;

        return expression.equals(that.expression)
                && !(factory != null ? !factory.equals(that.factory) : that.factory != null);

    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + (factory != null ? factory.hashCode() : 0);
        return result;
    }
}
