package org.asoem.sico.core.conditions;


import java.util.List;

import org.asoem.sico.core.individual.GFComponent;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.ConfigurableValueProvider;
import org.simpleframework.xml.Root;

@Root
public interface GFCondition extends GFComponent, ConfigurableValueProvider {

	public boolean evaluate(Simulation simulation);
	public boolean isLeafCondition();
	public boolean isRootCondition();
	
	public List<GFCondition> getChildConditions();
	public GFCondition getParentCondition();
	public GFCondition getRoot();
	
	public void setParent(GFCondition parent);
	public boolean add(GFCondition condition);
	public boolean remove(GFCondition condition);
}
