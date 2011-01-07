package org.asoem.greyfish.core.individual;


import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.DeepClonable;

public interface GFComponent extends Initializeable, DeepClonable {
	public Individual getComponentOwner();
	public void setComponentOwner(Individual individual);
	public void initialize(Simulation simulation);
	public void checkDependencies(Iterable<? extends GFComponent> components);
}
