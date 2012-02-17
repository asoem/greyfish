package org.asoem.greyfish.core.eval.impl;

import javax.script.*;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 17:02
 */
public class JavaScriptEvaluator extends JSR223Evaluator {

    private final ScriptEngine engine = manager.getEngineByName("JavaScript");

    @Override
    protected ScriptEngine engine() {
        assert engine != null;
        return engine;
    }

    @Override
    protected String prepare(String expression) {
        String ret = "importClass(org.asoem.greyfish.core.eval.GreyfishVariableFactory);\n";
        ret += expression.replaceAll("\\$\\(([^\\)]+)\\)", "GreyfishVariableFactory.\\$($1)");
        return ret;
    }
}
