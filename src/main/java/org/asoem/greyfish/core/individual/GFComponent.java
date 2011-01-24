package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.DeepClonable;

public interface GFComponent extends Initializeable, DeepClonable, Freezable, HasName {
	public Individual getComponentOwner();
	public void setComponentOwner(Individual individual);
	public void initialize(Simulation simulation);
	public void checkIfFreezable(Iterable<? extends GFComponent> components);
}
