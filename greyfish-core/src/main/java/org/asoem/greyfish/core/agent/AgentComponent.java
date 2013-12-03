package org.asoem.greyfish.core.agent;


import com.google.common.base.Optional;

import javax.annotation.Nullable;
import java.io.Serializable;

public interface AgentComponent<A extends Agent<A, ? extends SimulationContext<?>>> extends AgentNode, Serializable {

    /**
     * Get the agent this component was added to.
     *
     * @return the agent for this component
     */
    Optional<A> agent();

    /**
     * Sets the connected agent. This method should only be called by an Agent implementation in an addXXX method.
     *
     * @param agent the new agent
     */
    void setAgent(@Nullable A agent);

    /**
     * Set this components name to {@code name}
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * @return the name of this component
     */
    public String getName();
}
