package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.utils.GFComponents;
import org.asoem.greyfish.lang.BuilderInterface;
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
        GFComponents.rebaseAll(getComponents(), this);
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
        Agent clone = DeepCloner.startWith(agent, Agent.class);

        return new ImmutableAgent(
                clone.getPopulation(),
                clone.getProperties(),
                clone.getActions(),
                clone.getGenes(),
                clone.getBody());
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ImmutableAgent> {
        @Override
        public ImmutableAgent build() {
            return new ImmutableAgent(checkedSelf());
        }
        @Override
        protected Builder self() {
            return this;
        }
    }
}
