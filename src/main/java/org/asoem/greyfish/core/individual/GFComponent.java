package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.DeepCloneable;

public interface GFComponent extends Initializeable, Freezable, HasName, Iterable<GFComponent>, DeepCloneable {
	public IndividualInterface getComponentOwner();
	public void setComponentRoot(IndividualInterface individual);

    public void setName(String name);
    public boolean hasName(String s);

    @Override
	public void checkConsistency(Iterable<? extends GFComponent> components);


}
