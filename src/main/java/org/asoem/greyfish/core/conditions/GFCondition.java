package org.asoem.greyfish.core.conditions;


import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface GFCondition extends AgentComponent {

	public boolean evaluate(ParallelizedSimulation simulation);
	public boolean isLeafCondition();
	public boolean isRootCondition();
	
	public List<GFCondition> getChildConditions();
	@Nullable
    public GFCondition getParentCondition();
	public GFCondition getRoot();
	
	public void setParent(@Nullable GFCondition parent);
	public boolean add(GFCondition condition);
	public boolean remove(GFCondition condition);
}
