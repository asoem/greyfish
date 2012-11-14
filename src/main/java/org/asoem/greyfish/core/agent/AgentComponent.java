package org.asoem.greyfish.core.agent;


import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.gui.Configurable;
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.Nullable;
import java.io.Serializable;

public interface AgentComponent<A extends Agent<S, A, Z, P>, S extends Simulation<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> extends HasName, Configurable, AgentNode, Serializable {

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
