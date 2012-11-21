package org.asoem.greyfish.core.agent;


import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.gui.Configurable;

import javax.annotation.Nullable;
import java.io.Serializable;

public interface AgentComponent<A extends Agent<A, ?, ?>> extends HasName, Configurable, AgentNode, Serializable {

    /**
     * Get the agent this component is part of.
     * @return the connected agent
     */
    @Nullable A getAgent();

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
}
