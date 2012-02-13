package org.asoem.greyfish.core.eval;

import javax.script.*;

/**
 * User: christoph
 * Date: 13.02.12
 * Time: 16:44
 */
public abstract class ScriptEngineEvaluator implements Evaluator {

    private String expression;
    private VariableResolver resolver;
    private CompiledScript compiledScript;
    private Bindings bindings = new SimpleBindings();
    protected final ScriptEngineManager manager = new ScriptEngineManager();

    protected abstract ScriptEngine engine();

    @Override
    public double evaluateAsDouble() throws EvaluationException {
        return evaluateAs(Double.class);
    }

    @Override
    public boolean evaluateAsBoolean() throws EvaluationException {
        return evaluateAs(Boolean.class);
    }

    private <T> T evaluateAs(Class<T> clazz) throws EvaluationException {
        try {
            return clazz.cast(compiledScript.eval(bindings));
        } catch (ScriptException e) {
            throw new EvaluationException(e);
        } catch (ClassCastException e) {
            throw new EvaluationException("Script's return value cannot be cast to " + clazz);
        }
    }

    @Override
    public String evaluateAsString() throws EvaluationException {
        return evaluateAs(String.class);
    }

    @Override
    public void setExpression(String expression) throws SyntaxException {
        this.expression = prependImports(expression);
        try {
            this.compiledScript = ((Compilable) engine()).compile(this.expression);
        } catch (ScriptException e) {
            throw new SyntaxException(e);
        }
    }

    protected String prependImports(String expression) {
        return expression;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setResolver(VariableResolver resolver) {
        this.resolver = resolver;
        this.bindings = resolver.bindings();
    }

    @Override
    public VariableResolver getResolver() {
        return resolver;
    }
}
