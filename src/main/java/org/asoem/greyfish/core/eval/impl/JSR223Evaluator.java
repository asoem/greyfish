package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.*;

import javax.script.*;

/**
 * User: christoph
 * Date: 13.02.12
 * Time: 16:44
 */
public abstract class JSR223Evaluator implements Evaluator {

    private CompiledScript compiledScript;
    private Bindings bindings = new SimpleBindings();
    protected final ScriptEngineManager manager = new ScriptEngineManager();

    protected abstract ScriptEngine engine();

    @Override
    public EvaluationResult evaluate() throws EvaluationException {
        try {
            return new GenericEvaluationResult(compiledScript.eval(bindings));
        } catch (ScriptException e) {
            throw new EvaluationException(e);
        }
    }

    @Override
    public void setExpression(String expression) throws SyntaxException {
        String expression1 = prepare(expression);
        try {
            this.compiledScript = ((Compilable) engine()).compile(expression1);
        } catch (ScriptException e) {
            throw new SyntaxException(e);
        }
    }

    protected String prepare(String expression) {
        return expression;
    }

    @Override
    public void setResolver(VariableResolver resolver) {
        this.bindings = resolver.bindings();
    }

}
