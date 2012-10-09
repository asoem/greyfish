package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.GeneComponentList;
import org.asoem.greyfish.core.genes.ImmutableGeneComponentList;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * An {@code ImmutableAgent} is an implementation of an {@link Agent} which guarantees no structural changes during it's lifetime.
 * This means, you cannot createChildNode or remove any {@link AgentComponent} to this {@code Agent}. If you try to, it will throw an {@link UnsupportedOperationException}.
 * The {@code AgentComponent}s themselves are not restricted in their mutabilty, and they most certainly will always be mutable.
 * Apart from the structure, other properties of the {@code Agent} will be modified during a {@link Simulation}.
 */
public class ImmutableAgent extends AbstractAgent {

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableAgent(@Element(name = "body") Body body,
                           @Element(name = "properties") ComponentList<AgentProperty<?>> properties,
                           @Element(name = "actions") ComponentList<AgentAction> actions,
                           @Element(name = "agentTraitList") GeneComponentList<AgentTrait<?>> agentTraitList) {
        super(body, properties, actions, agentTraitList, createDefaultActionExecutionStrategyFactory());
        freeze();
    }

    private static ActionExecutionStrategyFactory createDefaultActionExecutionStrategyFactory() {
        return new ActionExecutionStrategyFactory() {
            @Override
            public ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions) {
                return new DefaultActionExecutionStrategy(actions);
            }
        };
    }

    private ImmutableAgent(ImmutableAgent agent, DeepCloner cloner) {
        super(agent, cloner);
    }

    private ImmutableAgent(Builder builder) {
        super(new Body(),
                ImmutableComponentList.copyOf(builder.properties),
                ImmutableComponentList.copyOf(builder.actions),
                ImmutableGeneComponentList.copyOf(builder.traits), null);
        setPopulation(builder.population);
        freeze();
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableAgent(this, cloner);
    }

    /**
     * Create a new Immutable Agent which is a copy of a deep clone of {@code agent}.
     * This means, that the Agent is deeply cloned first and the clone is used as the template for a flat copy.
     *
     * @param agent the agent to clone
     * @return a new ImmutableAgent initialized from a deep clone of {@code agent}
     */
    public static ImmutableAgent fromPrototype(Agent agent) {
        checkNotNull(agent);
        final Agent clone = DeepCloner.clone(agent, Agent.class);

        final ImmutableAgent ret = new ImmutableAgent(
                clone.getBody(),
                ImmutableComponentList.copyOf(clone.getProperties()),
                ImmutableComponentList.copyOf(clone.getActions()),
                ImmutableGeneComponentList.copyOf(clone.getTraits()));
        ret.setPopulation(clone.getPopulation());
        ret.setMotion(clone.getMotion());
        ret.setProjection(clone.getProjection());
        ret.setColor(clone.getColor());

        return ret;
    }

    public static Builder of(Population population) {
        return new Builder(population);
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

    public static final class Builder extends AbstractAgent.AbstractBuilder<ImmutableAgent, Builder> {
        public Builder(Population population) {
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

    @Override
    public boolean addGene(AgentTrait<?> gene) {
        throw new UnsupportedOperationException();
    }
}
