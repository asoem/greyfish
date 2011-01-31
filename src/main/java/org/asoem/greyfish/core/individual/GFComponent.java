package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.DeepClonable;

public interface GFComponent extends Initializeable, Freezable, HasName, Iterable<GFComponent>, DeepClonable {
	public IndividualInterface getComponentOwner();
	public void setComponentRoot(IndividualInterface individual);

    @Override
	public void checkConsistency(Iterable<? extends GFComponent> components);


}
