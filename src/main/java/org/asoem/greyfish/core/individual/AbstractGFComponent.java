package org.asoem.sico.core.individual;

import java.util.Map;

import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.HasName;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Attribute;

import com.google.common.base.Preconditions;

public abstract class AbstractGFComponent extends AbstractDeepCloneable implements GFComponent, HasName {

	protected Individual componentOwner;

	@Attribute(name="name", required = false)
	protected String name = "";

	public AbstractGFComponent() {
	}
	
	public AbstractGFComponent(String name) {
		this.name = name;
	}

	protected AbstractGFComponent(AbstractGFComponent component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(component, mapDict);
		this.name = component.getName();
		this.componentOwner = deepClone(component.getComponentOwner(), mapDict);
	}

	@Override
	public Individual getComponentOwner() {
		return componentOwner;
	}

	@Override
	public void setComponentOwner(Individual individual) {
		componentOwner = individual;
	}

	public void setName(String name) {
		Preconditions.checkNotNull(name);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public void initialize(Simulation simulation) {
		Preconditions.checkNotNull(componentOwner, name + " (" + this.getClass().getSimpleName() + "): Missing component owner is reqiured");
	}
	
//	@Override
//	public boolean isCloneOf(IndividualComponent component) {
//		return this.getClass().isInstance(component)
//			&& this.name.equals(component.getParameterName());
//	}
	
	@Override
	public void checkDependencies(Iterable<? extends GFComponent> components) {	
	}
}
