package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.Preparable;

import javax.annotation.Nullable;

public interface GFComponent extends Preparable<Simulation>, Freezable, HasName, Iterable<GFComponent>, DeepCloneable {


    @Nullable public Agent getAgent();
    void setAgent(@Nullable Agent agent);

    public void setName(String name);
    public boolean hasName(String s);

	public void checkConsistency(); // TODO: Rethink about this function. Feels wrong somehow.
    public void configure(ConfigurationHandler e);

    public void accept(ComponentVisitor visitor);
}
