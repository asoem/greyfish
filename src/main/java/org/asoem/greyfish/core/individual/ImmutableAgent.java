package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;

import javax.annotation.Nullable;


/**
 * User: christoph
 * Date: 19.09.11
 * Time: 10:42
 */
public class ImmutableAgent extends AbstractAgent {

    private ImmutableAgent(Population population, Iterable<GFProperty> properties, Iterable<GFAction> actions, Iterable<Gene<?>> genes, Body body) {
        super(body,
                ImmutableComponentList.copyOf(properties),
                ImmutableComponentList.copyOf(actions),
                ImmutableGenome.copyOf(Iterables.transform(genes, new Function<Gene<?>, Gene<?>>() {
                    @Override
                    public Gene<?> apply(@Nullable Gene<?> gene) {
                        return ForwardingGene.newInstance(gene);
                    }
                })));
        setPopulation(population);
    }

    protected ImmutableAgent(ImmutableAgent agent, DeepCloner map) {
        super(agent, map);
    }

    public ImmutableAgent(Builder builder) {
        super(new Body(),
                ImmutableComponentList.copyOf(builder.properties),
                ImmutableComponentList.copyOf(builder.actions),
                ImmutableGenome.copyOf(Iterables.transform(builder.genes, new Function<Gene<?>, Gene<?>>() {
                    @Override
                    public Gene<?> apply(@Nullable Gene<?> gene) {
                        return ForwardingGene.newInstance(gene);
                    }
                })));
        setPopulation(builder.population);
    }

    @Override
    public Genome createGamete() {
        return ImmutableGenome.copyOf(Iterables.transform(genome, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(@Nullable Gene<?> gene) {
                return ImmutableGene.copyOf(gene);
            }
        }));
    }

    @Override
    public void injectGamete(Genome genome) {

    }

    @Override
    public void prepare(Simulation context) {
        super.prepare(context);
        setSimulation(context);
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
