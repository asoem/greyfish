package org.asoem.greyfish.core.conditions;


import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface ActionCondition<A extends Agent<?, A, ?>> extends AgentComponent {

    AgentAction<A> getAction();
    void setAction(AgentAction<A> action);

	List<ActionCondition<A>> getChildConditions();
    ActionCondition<A> getRoot();
    void setParent(@Nullable ActionCondition<A> parent);
    ActionCondition getParent();

    void insert(ActionCondition<A> condition, int index);
    void add(ActionCondition<A> condition);
	void remove(ActionCondition<A> condition);
    void removeAll();

    boolean isLeafCondition();
	boolean isRootCondition();

    boolean evaluate();
}
