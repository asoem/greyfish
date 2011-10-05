package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 12:11
 */
public class GOMVariable {

    private final Class<? extends AgentComponent> context;
    private final String expression;

    public GOMVariable(String expression, Class<? extends AgentComponent> context) {
        this.expression = expression;
        this.context = context;
    }

    public static GOMVariable create(Class<? extends AgentComponent> contextClass, String varName) {
        return new GOMVariable(varName, contextClass);
    }
}
