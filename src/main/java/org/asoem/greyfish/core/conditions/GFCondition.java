package org.asoem.greyfish.core.conditions;


import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface GFCondition extends AgentComponent {

    @Nullable
    GFCondition getParentCondition();
	List<GFCondition> getChildConditions();
	GFCondition getRoot();

	void setParent(@Nullable GFCondition parent);
    void insert(GFCondition condition, int index);
    void add(GFCondition condition);
	void remove(GFCondition condition);
    void removeAll();

    boolean isLeafCondition();
	boolean isRootCondition();

    boolean evaluate(Simulation simulation);
}
