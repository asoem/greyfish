package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 21:57
 */
abstract class AbstractVariableResolver implements VariableResolver {

    private GFComponent context;

    @Override
    public GFComponent getContext() {
        return context;
    }

    @Override
    public void setContext(GFComponent context) {
        this.context = context;
    }
}
