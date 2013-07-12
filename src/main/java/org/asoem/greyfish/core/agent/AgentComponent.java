package org.asoem.greyfish.core.agent;


import javax.annotation.Nullable;
import java.io.Serializable;

public interface AgentComponent<A extends Agent<A, ?>> extends AgentNode, Serializable {

    /**
     * Get the agent this component was added to or {@code null} if it was not.
     * @return the agent for this component
     */
    @Nullable A getAgent();

    /**
     * Get the agent this component was added to.
     * This method throws an {@code IllegalStateException} if the agent is {@code null}.
     * @return the agent for this component
     */
    A agent() throws IllegalStateException;

    /**
     * Sets the connected agent. This method should only be called by an Agent implementation in an addXXX method.
     * @param agent the new agent
     */
    void setAgent(@Nullable A agent);

    /**
     * Set this components name to {@code name}
     * @param name the new name
     */
    void setName(String name);

    /**
     * @return the name of this component
     */
    public String getName();
}
