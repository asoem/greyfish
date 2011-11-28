package org.asoem.greyfish.core.conditions;


import com.google.common.base.Predicate;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface GFCondition extends AgentComponent, Predicate<Simulation> {

    @Nullable
    public GFCondition getParentCondition();
	public List<GFCondition> getChildConditions();
	public GFCondition getRoot();
	
	public void setParent(@Nullable GFCondition parent);
	public void add(GFCondition condition);
	public void remove(GFCondition condition);

    public boolean isLeafCondition();
	public boolean isRootCondition();
}
