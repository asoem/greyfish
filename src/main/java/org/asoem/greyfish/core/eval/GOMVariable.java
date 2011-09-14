package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 12:11
 */
public class GOMVariable {

    private final GFComponent context;
    private final String expression;

    public GOMVariable(String expression, GFComponent context) {
        this.expression = expression;
        this.context = context;
    }
}
