package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Freezable;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.collect.TreeNode;
import org.asoem.greyfish.utils.gui.Configurable;

import javax.annotation.Nullable;

public interface AgentComponent extends TreeNode<AgentComponent>, Freezable, HasName, DeepCloneable, Configurable {

    /**
     * Get the agent this component is part of.
     * @return the connected agent
     */
    @Nullable public Agent getAgent();

    /**
     * Sets the connected agent. This method should only be called by an Agent implementation in an addXXX method.
     * @param agent the new agent
     */
    void setAgent(@Nullable Agent agent);

    /**
     * Set this components name to {@code name}
     * @param name the new name
     */
    public void setName(String name);

    /**
     * The 'visitable' part of of the visitor pattern.
     * All implementations pass themselves to the visitor with {@code visitor.visit(this)}
     * @param visitor the visitor
     */
    public void accept(ComponentVisitor visitor);

    /**
     * Called by an {@code Agent} if it's {@code AgentComponent}s should prepare themselves for a new {@code Simulation} simulation.
     * Implementations should reset their own fields to an initial state and their those of their {@code super} class if they have any.
     */
    void initialize();
}
