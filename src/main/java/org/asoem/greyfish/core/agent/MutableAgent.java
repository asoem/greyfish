package org.asoem.greyfish.core.agent;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.List;

public class MutableAgent<S extends Simulation<S, A, ?, P>, A extends Agent<A, S, P>, P extends Object2D> extends BasicAgent<S,A,P> {

    private MutableAgent(MutableAgent<S,A,P> mutableAgent, DeepCloner map) {
        super(mutableAgent, map, INITIALIZATION_FACTORY);
    }

    private MutableAgent(Builder<A, S, P> builder) {
        super(builder, INITIALIZATION_FACTORY);
    }

    @Override
    public String toString() {
        return "MutableAgent[" + getPopulation() + "]";
    }

    @Override
    protected A self() {
        return null;
    }

    @Override
    public void changeActionExecutionOrder(final AgentAction<A> object, final AgentAction<A> object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! getActions().contains(object) || ! getActions().contains(object2))
            throw new IllegalArgumentException();
        int index1 = getActions().indexOf(object);
        int index2 = getActions().indexOf(object2);
        getActions().add(index2, getActions().remove(index1));
    }

    @Override
    public MutableAgent<S,A,P> deepClone(DeepCloner cloner) {
        return new MutableAgent<S,A,P>(this, cloner);
    }

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder<A extends Agent<A, S, P>, S extends Simulation<S,A,?,P>, P extends Object2D> extends AbstractBuilder<A,S,P,MutableAgent<S,A,P>,Builder<A,S,P>> {
        private Builder(Population population) {
            super(population);
        }

        @Override
        protected MutableAgent<S,A,P> checkedBuild() {
            return new MutableAgent<S,A,P>(this);
        }
        @Override
        protected Builder<A,S,P> self() {
            return this;
        }
    }

    private static final AgentInitializationFactory INITIALIZATION_FACTORY = new AgentInitializationFactory() {
        @Override
        public ActionExecutionStrategy createStrategy(List<? extends AgentAction<?>> actions) {
            return new DefaultActionExecutionStrategy(actions);
        }

        @Override
        public <T extends AgentComponent> SearchableList<T> newSearchableList(Iterable<T> elements) {
            return AugmentedLists.newAugmentedArrayList(elements);
        }

        @Override
        public <A extends Agent> AgentMessageBox<A> createMessageBox() {
            return new FixedSizeMessageBox<A>();
        }
    };
}
