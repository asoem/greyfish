package org.asoem.greyfish.core.conditions;


import com.google.common.base.Predicate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface GFCondition extends AgentComponent, Predicate<AgentAction> {

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

    @Override
    boolean apply(AgentAction action);
}
