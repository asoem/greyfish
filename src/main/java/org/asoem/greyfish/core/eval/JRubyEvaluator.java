package org.asoem.greyfish.core.eval;

import javax.script.*;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 17:02
 */
public class JRubyEvaluator extends ScriptEngineEvaluator {

    private final ScriptEngine engine = manager.getEngineByName("jruby");

    @Override
    protected ScriptEngine engine() {
        assert engine != null;
        return engine;
    }

    @Override
    protected String prependImports(String expression) {
        return "include Java\n" +
                "java_import org.asoem.greyfish.core.eval.GreyfishVariableFactory\n" +
                expression.replaceAll("\\$\\(([^\\)]+)\\)", "GreyfishVariableFactory.\\dollar($1)");
    }
}
