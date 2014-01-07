package org.asoem.greyfish.core.properties;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.collect.RangeElement;

import javax.annotation.Nullable;

public interface RangeElementProperty<A extends Agent<?>, T extends Number & Comparable<T>, C extends AgentContext<A>> extends AgentProperty<C, T>, RangeElement<T> {
    /**
     * Sets the connected agent. This method should only be called by an Agent implementation in an addXXX method.
     *
     * @param agent the new agent
     */
    void setAgent(@Nullable A agent);

    /**
     * Get the agent this component was added to.
     *
     * @return the agent for this component
     */
    Optional<A> agent();
}
