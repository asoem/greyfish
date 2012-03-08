package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.ImmutableChromosome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;

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
                             @Element(name = "properties") ComponentList<GFProperty> properties,
                             @Element(name = "actions") ComponentList<GFAction> actions,
                             @Element(name = "chromosome") Chromosome<Gene<?>> chromosome) {
        super(body, properties, actions, chromosome);
        freeze();
    }

    private ImmutableAgent(ImmutableAgent agent, DeepCloner map) {
        super(agent, map);
    }

    private ImmutableAgent(Builder builder) {
        super(new Body(),
                ImmutableComponentList.copyOf(builder.properties),
                ImmutableComponentList.copyOf(builder.actions),
                ImmutableChromosome.copyOf(builder.genes));
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
     * @param agent the agent to clone
     * @return a new ImmutableAgent initialized from a deep clone of {@code agent}
     */
    public static ImmutableAgent cloneOf(Agent agent) {
        checkNotNull(agent);
        Agent clone = DeepCloner.clone(agent, Agent.class);

        final ImmutableAgent ret = new ImmutableAgent(
                clone.getBody(),
                clone.getProperties(),
                clone.getActions(),
                ImmutableChromosome.copyOf(clone.getChromosome()));
        ret.setPopulation(clone.getPopulation());
        ret.setMotion(clone.getMotion());
        ret.setProjection(clone.getProjection());
        ret.setSimulationContext(agent.getSimulationContext());
        ret.setColor(clone.getColor());

        return ret;
    }

    public static Builder of(Population population) {
        return new Builder(population);
    }

    @Override
    public void freeze() {
        for(AgentComponent component : getComponents())
            component.freeze();
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
            return new ImmutableAgent(this);
        }
        @Override
        protected Builder self() {
            return this;
        }
    }

    @Override
    public boolean addGene(Gene<?> gene) {
        throw new UnsupportedOperationException();
    }
}
