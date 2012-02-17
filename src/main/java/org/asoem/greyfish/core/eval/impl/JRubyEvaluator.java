package org.asoem.greyfish.core.eval.impl;

import javax.script.*;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 17:02
 */
public class JRubyEvaluator extends JSR223Evaluator {

    private final ScriptEngine engine = manager.getEngineByName("jruby");

    @Override
    protected ScriptEngine engine() {
        assert engine != null;
        return engine;
    }

    @Override
    protected String prepare(String expression) {
        return "include Java\n" +
                "java_import org.asoem.greyfish.core.eval.GreyfishVariableFactory\n" +
                expression.replaceAll("\\$\\(([^\\)]+)\\)", "GreyfishVariableFactory.\\dollar($1)");
    }
}
