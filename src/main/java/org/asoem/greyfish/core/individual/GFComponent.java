package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.Configurable;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.Preparable;

import javax.annotation.Nullable;

public interface GFComponent extends Preparable<Simulation>, Freezable, HasName, Iterable<GFComponent>, DeepCloneable, Configurable {

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
     * Check if the component is consistent or has any errors.
     * @throws IllegalStateException if some error was detected
     */
    // TODO: Rethink this function. Feels wrong somehow.
	public void checkConsistency() throws IllegalStateException;

    /**
     * The 'visitable' part of of the visitor pattern.
     * All implementations pass themselves to the visitor with {@code visitor.visit(this)}
     * @param visitor the visitor
     */
    public void accept(ComponentVisitor visitor);

    /**
     *
     * @return all components in the component tree (of the agent) which this component is part of.
     * It returns an empty iterable if {@link #getAgent} returns {@code null}
     */
    public Iterable<GFComponent> getAllComponents();
}
