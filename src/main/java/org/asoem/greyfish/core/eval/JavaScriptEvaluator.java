package org.asoem.greyfish.core.eval;

import javax.script.*;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 17:02
 */
public class JavaScriptEvaluator implements Evaluator {

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName("JavaScript");
    private String expression;
    private VariableResolver resolver;
    private CompiledScript compiledScript;
    private Bindings bindings = new SimpleBindings();

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
            this.compiledScript = ((Compilable) engine).compile(this.expression);
        } catch (ScriptException e) {
            throw new SyntaxException(e);
        }
    }

    private String prependImports(String expression) {
        String ret = "importClass(org.asoem.greyfish.core.eval.GreyfishVariableFactory);\n";
        ret += expression.replaceAll("\\$\\(([^\\)]+)\\)", "GreyfishVariableFactory.\\$($1)");
        return ret;
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
