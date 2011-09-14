package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 18.05.11
 * Time: 17:00
 */
public interface VariableResolver {
    Object resolve(String varName);
    GFComponent getContext();
    void setContext(GFComponent context);
}
