package org.asoem.greyfish.core.conditions;


import com.google.common.base.Predicate;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.collect.TreeNode;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface GFCondition extends AgentComponent, Predicate<Simulation> {

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

}
