package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.DeepClonable;

public interface GFComponent extends Initializeable, DeepClonable, Freezable, HasName, Iterable<GFComponent> {
	public Individual getComponentOwner();
	public void setComponentRoot(Individual individual);

    @Override
	public void checkConsistency(Iterable<? extends GFComponent> components);


}
