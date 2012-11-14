package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collections;
import java.util.List;

public abstract class LeafCondition<A extends Agent<S, A, Z, P>, S extends Simulation<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> extends AbstractCondition<A,S,Z,P> {

    protected LeafCondition() {}

    protected LeafCondition(LeafCondition cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    protected LeafCondition(AbstractBuilder<? extends LeafCondition, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    @Override
	public final List<ActionCondition<A,S,Z,P>> getChildConditions() {
		return Collections.emptyList();
	}

	@Override
	public final boolean isLeafCondition() {
		return true;
	}

    @Override
    public final Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public final void remove(ActionCondition<A,S,Z,P> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(ActionCondition<A,S,Z,P> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(ActionCondition<A,S,Z,P> condition, int index) {
        throw new UnsupportedOperationException();
    }

    protected static abstract class AbstractBuilder<C extends AbstractCondition, B extends AbstractBuilder<C, B>> extends AbstractCondition.AbstractBuilder<C, B> {
        protected AbstractBuilder(LeafCondition leafCondition) {
            super(leafCondition);
        }

        protected AbstractBuilder() {
        }
    }
}
