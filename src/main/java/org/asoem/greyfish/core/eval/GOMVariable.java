package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 12:11
 */
public class GOMVariable {

    private final Class<? extends GFComponent> context;
    private final String expression;

    public GOMVariable(String expression, Class<? extends GFComponent> context) {
        this.expression = expression;
        this.context = context;
    }

    public static GOMVariable create(Class<? extends GFComponent> contextClass, String varName) {
        return new GOMVariable(varName, contextClass);
    }
}
