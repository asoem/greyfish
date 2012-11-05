package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.List;


/**
 * An {@code ImmutableAgent} is an implementation of an {@link Agent} which guarantees no structural changes during it's lifetime.
 * This means, you cannot createChildNode or remove any {@link AgentComponent} to this {@code Agent}. If you try to, it will throw an {@link UnsupportedOperationException}.
 * The {@code AgentComponent}s themselves are not restricted in their mutabilty, and they most certainly will always be mutable.
 * Apart from the structure, other properties of the {@code Agent} will be modified during a {@link Simulation}.
 */
public class ImmutableAgent extends AbstractAgent {

    private ImmutableAgent(ImmutableAgent agent, DeepCloner cloner) {
        super(agent, cloner);
        freeze();
    }

    private ImmutableAgent(Builder builder) {
        super(builder);
        freeze();
    }

    @Override
    public ImmutableAgent deepClone(DeepCloner cloner) {
        return new ImmutableAgent(this, cloner);
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

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder extends AbstractAgent.AbstractBuilder<ImmutableAgent, Builder> {
        private Builder(Population population) {
            super(population, createDefaultInitializationFactory());

        }

        @Override
        protected ImmutableAgent checkedBuild() {
            final ImmutableAgent agent = new ImmutableAgent(this);
            agent.initialize();
            return agent;
        }

        @Override
        protected Builder self() {
            return this;
        }

        private static AgentInitializationFactory createDefaultInitializationFactory() {
            return new AgentInitializationFactory() {
                @Override
                public ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions) {
                    return new DefaultActionExecutionStrategy(actions);
                }

                @Override
                public <T extends AgentComponent> ComponentList<T> createComponentList(Iterable<T> elements) {
                    return ImmutableComponentList.copyOf(elements);
                }

                @Override
                public AgentMessageBox createMessageBox() {
                    return new FixedSizeMessageBox();
                }
            };
        }
    }
}
