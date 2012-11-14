package org.asoem.greyfish.core.conditions;


import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.List;

@Root
public interface ActionCondition<A extends Agent<S, A, Z, P>, S extends Simulation<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> extends AgentComponent {

    AgentAction<A,S,Z,P> getAction();
    void setAction(AgentAction<A,S,Z,P> action);

	List<ActionCondition<A,S,Z,P>> getChildConditions();
    ActionCondition<A,S,Z,P> getRoot();
    void setParent(@Nullable ActionCondition<A,S,Z,P> parent);
    ActionCondition getParent();

    void insert(ActionCondition<A,S,Z,P> condition, int index);
    void add(ActionCondition<A,S,Z,P> condition);
	void remove(ActionCondition<A,S,Z,P> condition);
    void removeAll();

    boolean isLeafCondition();
	boolean isRootCondition();

    boolean evaluate();
}
