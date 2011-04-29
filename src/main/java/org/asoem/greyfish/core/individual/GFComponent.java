package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.Preparable;

public interface GFComponent extends Preparable<Simulation>, Freezable, HasName, Iterable<GFComponent>, DeepCloneable {
	public IndividualInterface getComponentOwner();
	public void setComponentRoot(IndividualInterface individual);

    public void setName(String name);
    public boolean hasName(String s);

    @Override
	public void checkConsistency(Iterable<? extends GFComponent> components);


}
