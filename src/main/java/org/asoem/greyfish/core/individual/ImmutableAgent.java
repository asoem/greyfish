package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * An {@code ImmutableAgent} is an implementation of an {@link Agent} which guarantees no structural changes during it's lifetime.
 * This means, you cannot createChildNode or remove any {@link AgentComponent} to this {@code Agent}. If you try to, it will throw an {@link UnsupportedOperationException}.
 * The {@code AgentComponent}s themselves are not restricted in their mutabilty, and they most certainly will always be mutable.
 * Apart from the structure, other properties of the {@code Agent} will be modified during a {@link Simulation}.
 */
public class ImmutableAgent extends AbstractAgent {

    private ImmutableAgent(Population population, Iterable<GFProperty> properties, Iterable<GFAction> actions, Iterable<Gene<?>> genes, Body body) {
        super(body,
                ImmutableComponentList.copyOf(properties),
                ImmutableComponentList.copyOf(actions),
                ImmutableGenome.copyOf(Iterables.transform(genes, new Function<Gene<?>, ForwardingGene<?>>() {
                    @Override
                    public ForwardingGene<?> apply(@Nullable Gene<?> gene) {
                        return ForwardingGene.newInstance(gene);
                    }
                })));
        setPopulation(population);
    }

    private ImmutableAgent(ImmutableAgent agent, DeepCloner map) {
        super(agent, map);
    }

    private ImmutableAgent(Builder builder) {
        super(new Body(),
                ImmutableComponentList.copyOf(builder.properties),
                ImmutableComponentList.copyOf(builder.actions),
                ImmutableGenome.copyOf(Iterables.transform(builder.genes, new Function<Gene<?>, ForwardingGene<?>>() {
                    @Override
                    public ForwardingGene<?> apply(@Nullable Gene<?> gene) {
                        return ForwardingGene.newInstance(gene);
                    }
                })));
        setPopulation(builder.population);
    }

    @Override
    public Genome<Gene<?>> createGamete() {
        return ImmutableGenome.copyOf(Iterables.transform(genome, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(@Nullable Gene<?> gene) {
                return ImmutableGene.copyOf(gene);
            }
        }));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableAgent(this, cloner);
    }

    /**
     * Create a "quasi" deep clone of {@code agent}.
     * This means, that all components are deep cloned from {@code agent}, but the agent itself is a new ImmutableAgent.
     * @param agent the prototype from which to clone the components
     * @return a new ImmutableAgent with components deep cloned from {@code agent}
     */
    public static ImmutableAgent cloneOf(Agent agent) {
        checkNotNull(agent);
        Agent clone = DeepCloner.clone(agent, Agent.class);

        return new ImmutableAgent(
                clone.getPopulation(),
                clone.getProperties(),
                clone.getActions(),
                clone.getGenes(),
                clone.getBody());
    }

    public static Builder of(Population population) {
        return new Builder(population);
    }

    public static final class Builder extends AbstractAgent.AbstractBuilder<ImmutableAgent, Builder> {
        public Builder(Population population) {
            super(population);
        }

        @Override
        public ImmutableAgent checkedBuild() {
            return new ImmutableAgent(this);
        }
        @Override
        protected Builder self() {
            return this;
        }
    }
}
