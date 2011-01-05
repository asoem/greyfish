package org.asoem.sico.core.individual;


import org.asoem.sico.core.simulation.Initializeable;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.DeepClonable;

public interface GFComponent extends Initializeable, DeepClonable {
	public Individual getComponentOwner();
	public void setComponentOwner(Individual individual);
	public void initialize(Simulation simulation);
	public void checkDependencies(Iterable<? extends GFComponent> components);
}
