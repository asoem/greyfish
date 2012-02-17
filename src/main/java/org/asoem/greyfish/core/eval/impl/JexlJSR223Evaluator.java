package org.asoem.greyfish.core.eval.impl;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.jexl2.scripting.JexlScriptEngine;

import javax.script.ScriptEngine;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 11:18
 */
public class JEXLJSR223Evaluator extends JSR223Evaluator {
    private final JexlScriptEngine engine = (JexlScriptEngine) manager.getEngineByName("jexl");

    public JEXLJSR223Evaluator() {
        final JexlScriptEngine.JexlScriptObject jexlScriptObject = engine.new JexlScriptObject();
        jexlScriptObject.getEngine().setFunctions(ImmutableMap.<String, Object>of("GreyfishVariableFactory", org.asoem.greyfish.core.eval.GreyfishVariableFactory.class));
    }

    @Override
    protected ScriptEngine engine() {
        return engine;
    }

    @Override
    protected String prepare(String expression) {
        return expression.replaceAll("\\$\\(([^\\)]+)\\)", "GreyfishVariableFactory:\\$($1)");
    }
}
