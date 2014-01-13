package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentProperty<C, T> extends AgentComponent<C> {

    /**
     * Get the value for this property in the given {@code context}.
     *
     * @param context the context for this trait
     * @return the value for the context
     */
    T value(final C context);
}
