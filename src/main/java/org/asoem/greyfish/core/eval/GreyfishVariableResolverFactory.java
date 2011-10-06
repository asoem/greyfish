package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 15:40
 */
public interface GreyfishVariableResolverFactory {

    <T extends AgentComponent> GreyfishVariableResolver<T> create(Class<T> contextClass);
}
