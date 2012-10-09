package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;


/**
 * An {@code ImmutableAgent} is an implementation of an {@link Agent} which guarantees no structural changes during it's lifetime.
 * This means, you cannot createChildNode or remove any {@link AgentComponent} to this {@code Agent}. If you try to, it will throw an {@link UnsupportedOperationException}.
 * The {@code AgentComponent}s themselves are not restricted in their mutabilty, and they most certainly will always be mutable.
 * Apart from the structure, other properties of the {@code Agent} will be modified during a {@link Simulation}.
 */
public class ImmutableAgent extends AbstractAgent {

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableAgent(@Element(name = "properties") ComponentList<AgentProperty<?>> properties,
                           @Element(name = "actions") ComponentList<AgentAction> actions,
                           @Element(name = "traits") ComponentList<AgentTrait<?>> agentTraitList,
                           AgentInitializationFactory factory) {
        super(properties, actions, agentTraitList, factory);
        freeze();
    }

    private ImmutableAgent(ImmutableAgent agent, DeepCloner cloner) {
        super(agent, cloner);
    }

    private ImmutableAgent(Builder builder) {
        super(ImmutableComponentList.copyOf(builder.properties),
                ImmutableComponentList.copyOf(builder.actions),
                ImmutableComponentList.copyOf(builder.traits),
                builder.agentInitializationFactory);
        setPopulation(builder.population);
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

    @Override
    public boolean addGene(AgentTrait<?> gene) {
        throw new UnsupportedOperationException();
    }

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder extends AbstractAgent.AbstractBuilder<ImmutableAgent, Builder> {
        private Builder(Population population) {
            super(population);
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
    }
}
