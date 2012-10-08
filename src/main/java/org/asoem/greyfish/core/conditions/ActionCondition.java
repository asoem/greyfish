package org.asoem.greyfish.core.conditions;


import com.google.common.base.Predicate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface ActionCondition extends AgentComponent, Predicate<AgentAction> {

    @Nullable
    ActionCondition getParentCondition();
	List<ActionCondition> getChildConditions();
	ActionCondition getRoot();

	void setParent(@Nullable ActionCondition parent);
    void insert(ActionCondition condition, int index);
    void add(ActionCondition condition);
	void remove(ActionCondition condition);
    void removeAll();

    boolean isLeafCondition();
	boolean isRootCondition();

    @Override
    boolean apply(AgentAction action);
}
