package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 18.05.11
 * Time: 17:00
 */
public interface GreyfishVariableResolver<T extends AgentComponent> extends VariableResolver {
    T getContext();
    void setContext(T context);
}
