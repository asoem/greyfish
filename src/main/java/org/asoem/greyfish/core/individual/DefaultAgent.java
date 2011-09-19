package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.MutableGenome;
import org.asoem.greyfish.core.io.AgentLog;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

@Root
public class DefaultAgent extends AbstractAgent {

    @NotNull
    protected SimulationContext simulationContext;

    protected DefaultAgent(DefaultAgent defaultAgent, CloneMap map) {
        super(defaultAgent, map);
    }

    protected DefaultAgent(AbstractBuilder<?> builder) {
        super(builder.body, builder.properties, builder.actions, new MutableGenome(builder.genes, this));
        setPopulation(builder.population);

        this.population = builder.population;
        this.body = Body.newInstance(this);
        this.genome = new MutableGenome(null);
        for (Gene<?> gene : builder.genes) {
            addGene(gene);
        }
        for (GFAction action : builder.actions) {
            addAction(action);
        }
        for (GFProperty property : builder.properties) {
            addProperty(property);
        }
    }

    @Override
    public AgentLog getLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutDown() {
        throw new UnsupportedOperationException();
    }


    @Override
    public <T extends GFAction> T getAction(final String actionName, Class<T> gfActionClass) {
        return Iterables.find(
                Iterables.filter(actions, gfActionClass),
                new Predicate<T>() {

                    @Override
                    public boolean apply(T object) {
                        return object.hasName(actionName);
                    }
                }, null);
    }


    @Override
    @Nullable
    public <T extends GFProperty> T getProperty(final String name, Class<T> propertyClass) {
        return Iterables.find(
                Iterables.filter(properties, propertyClass),
                new Predicate<T>() {

                    @Override
                    public boolean apply(T object) {
                        return object.hasName(name);
                    }
                }, null);
    }

    @Override
    public String toString() {
        return "DefaultAgent[" + population + "]";
    }

    @SuppressWarnings("unused")
    @Commit
    private void commit() {
        for (GFComponent component : getComponents()) {
            component.setAgent(this);
        }
    }

    @Override
    public boolean isCloneOf(Object object) {
        return Agent.class.isInstance(object)
                && population.equals(Agent.class.cast(object).getPopulation());
    }

    @Override
    public Iterable<GFComponent> getComponents() {
        return Iterables.concat( ImmutableList.of(body), properties, actions);
    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! actions.contains(object) || ! actions.contains(object2))
            throw new IllegalArgumentException();
        int index1 = actions.indexOf(object);
        int index2 = actions.indexOf(object2);
        actions.add(index2, actions.remove(index1));
    }

    @Override
    public void freeze() {
        for (GFComponent component : getComponents()) {
            component.checkConsistency();
            component.freeze();
        }
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void checkNotFrozen() {
        if (isFrozen()) throw new IllegalStateException("DefaultAgent is frozen");
    }

    @Override
    public void prepare(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        this.simulationContext = new SimulationContext(simulation, this);

        for (GFComponent component : this) {
            component.prepare(simulation);
        }
    }

    public Simulation getSimulation() {
        return simulationContext.getSimulation();
    }

    @Override
    public void setSimulation(Simulation simulation) {
        simulationContext = new SimulationContext(simulation, this); // TODO: Is 'this' sufficient? DELEGATION!
    }

    @Override
    protected SimulationContext getSimulationContext() {
        return simulationContext;
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new DefaultAgent(this, map);
    }

    public void setGenome(final Genome genome) {
        Preconditions.checkNotNull(genome);
        genome.reset(genome);
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DefaultAgent> {
        @Override
        public DefaultAgent build() {
            return new DefaultAgent(checkedSelf());
        }
        @Override
        protected Builder self() {
            return this;
        }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends org.asoem.greyfish.lang.AbstractBuilder<T> {
        private final List<GFAction> actions = Lists.newArrayList();
        private final List<GFProperty> properties =  Lists.newArrayList();
        private Population population;
        public final List<Gene<?>> genes = Lists.newArrayList();

        public T addGenes(Gene<?> ... genes) { this.genes.addAll(asList(checkNotNull(genes))); return self(); }
        public T population(Population population) { this.population = checkNotNull(population); return self(); }
        public T addActions(GFAction ... actions) { this.actions.addAll(asList(checkNotNull(actions))); return self(); }
        public T addProperties(GFProperty ... properties) { this.properties.addAll(asList(checkNotNull(properties))); return self(); }
    }
}
