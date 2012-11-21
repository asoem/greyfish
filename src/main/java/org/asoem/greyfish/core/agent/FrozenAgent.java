package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.Object2D;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;


/**
 * An {@code FrozenAgent} is an implementation of an {@link Agent} which guarantees no structural changes after construction.
 * This can prevent bugs but also allows for structure dependent performance optimizations.
 * This means, you cannot add or remove any {@link AgentComponent} to this {@code Agent}.
 * If you try to, it will throw an {@link UnsupportedOperationException}.
 * However, no guarantees can be made about the {@code AgentComponent}s themselves,
 * but should generally act according to the frozen state of their parent component.
 */
public class FrozenAgent<S extends Simulation<S, A, ?, P>, A extends Agent<A, S, P>, P extends Object2D> extends BasicAgent<S,A,P> implements Serializable {

    private FrozenAgent(FrozenAgent<S,A,P> agent, DeepCloner cloner) {
        super(agent, cloner, INITIALIZATION_FACTORY);
        freeze();
    }

    private FrozenAgent(Builder<S,A,P> builder) {
        super(builder, INITIALIZATION_FACTORY);
        freeze();
    }

    @Override
    public FrozenAgent<S, A, P> deepClone(DeepCloner cloner) {
        return new FrozenAgent<S, A, P>(this, cloner);
    }

    @Override
    protected A self() {
        return this;
    }

    @Override
    public void freeze() {
        for (AgentNode node : children())
            node.freeze();
    }

    @Override
    public boolean isFrozen() {
        return true;
    }

    private Object writeReplace() {
        return new Builder<S,A,P>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <S extends Simulation<S, A, ?, P>, A extends Agent<A, S, P>, P extends Object2D> Builder<S, A, P> builder(Population population) {
        return new Builder<S,A,P>(population);
    }

    public static class Builder<S extends Simulation<S, A, ?, P>, A extends Agent<A, S, P>, P extends Object2D> extends BasicAgent.AbstractBuilder<A, S, P, FrozenAgent<S,A,P>, Builder<S, A, P>> implements Serializable {
        private Builder(Population population) {
            super(population);
        }

        private Builder(FrozenAgent<S,A,P> frozenAgent) {
            super(frozenAgent);
        }

        @Override
        protected FrozenAgent<S,A,P> checkedBuild() {
            final FrozenAgent<S,A,P> agent = new FrozenAgent<S,A,P>(this);
            agent.initialize();
            return agent;
        }

        @Override
        protected Builder<S,A,P> self() {
            return this;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }

    private static final AgentInitializationFactory INITIALIZATION_FACTORY = new AgentInitializationFactory() {
        @Override
        public ActionExecutionStrategy createStrategy(List<? extends AgentAction<?>> actions) {
            return new DefaultActionExecutionStrategy(actions);
        }

        @Override
        public <T extends AgentComponent> SearchableList<T> newSearchableList(Iterable<T> elements) {
            return AugmentedLists.copyOf(elements);
        }

        @Override
        public <A extends Agent> AgentMessageBox<A> createMessageBox() {
            return new FixedSizeMessageBox<A>();
        }
    };
}
