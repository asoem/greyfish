package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.asoem.greyfish.utils.collect.SearchableList;

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
public class FrozenAgent extends AbstractAgent implements Serializable {

    private FrozenAgent(FrozenAgent agent, DeepCloner cloner) {
        super(agent, cloner, INITIALIZATION_FACTORY);
        freeze();
    }

    private FrozenAgent(Builder builder) {
        super(builder, INITIALIZATION_FACTORY);
        freeze();
    }

    @Override
    public FrozenAgent deepClone(DeepCloner cloner) {
        return new FrozenAgent(this, cloner);
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
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder extends AbstractAgent.AbstractBuilder<FrozenAgent, Builder> implements Serializable {
        private Builder(Population population) {
            super(population);
        }

        private Builder(FrozenAgent frozenAgent) {
            super(frozenAgent);
        }

        @Override
        protected FrozenAgent checkedBuild() {
            final FrozenAgent agent = new FrozenAgent(this);
            agent.initialize();
            return agent;
        }

        @Override
        protected Builder self() {
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
        public ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions) {
            return new DefaultActionExecutionStrategy(actions);
        }

        @Override
        public <T extends AgentComponent> SearchableList<T> newSearchableList(Iterable<T> elements) {
            return AugmentedLists.copyOf(elements);
        }

        @Override
        public AgentMessageBox createMessageBox() {
            return new FixedSizeMessageBox();
        }
    };
}
