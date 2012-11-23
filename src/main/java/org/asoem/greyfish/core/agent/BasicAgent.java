package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.SpatialSimulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 16:20
 */
abstract class BasicAgent<A extends Agent<A, S, P>, S extends SpatialSimulation<A, Z>, P extends Object2D, Z extends Space2D<A, P>> extends AbstractAgent<A, S, P> {

    private final SearchableList<AgentProperty<A, ?>> properties;
    private final SearchableList<AgentAction<A>> actions;
    private final SearchableList<AgentTrait<A, ?>> traits;
    private final ActionExecutionStrategy actionExecutionStrategy;
    private final AgentMessageBox<A> inBox;
    @Nullable
    private Population population;
    @Nullable
    private P projection;
    private Motion2D motion = ImmutableMotion2D.noMotion();
    private SimulationContext<S,A> simulationContext = PassiveSimulationContext.instance();
    private Set<Integer> parents = Collections.emptySet();
    private final Supplier<A> selfSupplier;

    protected BasicAgent(BasicAgent<A, S, P, Z> abstractAgent, final DeepCloner cloner, AgentInitializationFactory agentInitializationFactory) {
        cloner.addClone(abstractAgent, this);
        // share
        this.population = abstractAgent.population;
        // clone
        this.actions = agentInitializationFactory.newSearchableList(Iterables.transform(abstractAgent.actions, new Function<AgentAction<A>, AgentAction<A>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentAction<A> apply(@Nullable AgentAction<A> agentAction) {
                return (AgentAction<A>) cloner.getClone(agentAction);
            }
        }));
        this.properties = agentInitializationFactory.newSearchableList(Iterables.transform(abstractAgent.properties, new Function<AgentProperty<A, ?>, AgentProperty<A, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentProperty<A, ?> apply(@Nullable AgentProperty<A, ?> agentProperty) {
                return (AgentProperty<A, ?>) cloner.getClone(agentProperty);
            }
        }));
        this.traits = agentInitializationFactory.newSearchableList(Iterables.transform(abstractAgent.traits, new Function<AgentTrait<A, ?>, AgentTrait<A, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentTrait<A, ?> apply(@Nullable AgentTrait<A, ?> agentTrait) {
                return (AgentTrait<A, ?>) cloner.getClone(agentTrait);
            }
        }));
        // reconstruct
        this.actionExecutionStrategy = checkNotNull(agentInitializationFactory.createStrategy(actions));
        this.inBox = checkNotNull(agentInitializationFactory.<A>createMessageBox());
    }

    protected BasicAgent(AbstractBuilder<A, S, Z, P, ?, ?> builder, AgentInitializationFactory agentInitializationFactory) {
        this.properties = agentInitializationFactory.newSearchableList(builder.properties);
        for (AgentProperty<A, ?> property : builder.properties) {
            property.setAgent(self());
        }
        this.actions = agentInitializationFactory.newSearchableList(builder.actions);
        for (AgentAction<A> action : builder.actions) {
            action.setAgent(self());
        }
        this.traits = agentInitializationFactory.newSearchableList(builder.traits);
        for (AgentTrait<A, ?> trait : builder.traits) {
            trait.setAgent(self());
        }
        this.population = builder.population;
        this.actionExecutionStrategy = checkNotNull(agentInitializationFactory.createStrategy(actions));
        this.inBox = checkNotNull(agentInitializationFactory.<A>createMessageBox());
    }

    @Override
    protected A self() {
        return selfSupplier.get();
    }

    @Nullable
    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(@Nullable Population population) {
        this.population = population;
    }

    @Override
    public SearchableList<AgentTrait<A, ?>> getTraits() {
        return traits;
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public Motion2D getMotion() {
        return motion;
    }

    @Override
    public SearchableList<AgentProperty<A, ?>> getProperties() {
        return properties;
    }

    @Override
    public SearchableList<AgentAction<A>> getActions() {
        return actions;
    }

    @Nullable
    @Override
    public P getProjection() {
        return projection;
    }

    @Override
    public void setProjection(@Nullable P projection) {
        this.projection = projection;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public void setMotion(Motion2D motion) {
        this.motion = checkNotNull(motion);
    }

    @Override
    public String toString() {
        return "Agent[" + getPopulation() + ']' + "#" + getSimulationContext().getAgentId() + "@" + getSimulationContext().getSimulationStep();
    }

    @Override
    protected SimulationContext<S, A> getSimulationContext() {
        return simulationContext;
    }

    @Override
    protected AgentMessageBox<A> getInBox() {
        return inBox;
    }

    @Override
    protected void setSimulationContext(SimulationContext<S, A> simulationContext) {
        this.simulationContext = simulationContext;
    }

    @Override
    protected ActionExecutionStrategy getActionExecutionStrategy() {
        return actionExecutionStrategy;
    }

    @Override
    protected void setParents(Set<Integer> parents) {
        this.parents = parents;
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, S, P>, S extends SpatialSimulation<A,Z>, Z extends Space2D<A,P>, P extends Object2D, E extends BasicAgent<A, S, P, Z>, T extends AbstractBuilder<A,S,Z,P,E,T>> extends InheritableBuilder<E, T> implements Serializable {
        private final Population population;
        private final List<AgentAction<A>> actions = Lists.newArrayList();
        private final List<AgentProperty<A, ?>> properties = Lists.newArrayList();
        private final List<AgentTrait<A, ?>> traits = Lists.newArrayList();

        protected AbstractBuilder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        protected AbstractBuilder(BasicAgent<A, S, P, Z> abstractAgent) {
            this.population = abstractAgent.population;
            this.actions.addAll(abstractAgent.actions);
            this.properties.addAll(abstractAgent.properties);
            this.traits.addAll(abstractAgent.traits);
        }

        public T addTraits(AgentTrait<A, ?>... traits) {
            this.traits.addAll(asList(checkNotNull(traits)));
            return self();
        }

        public T addTraits(Iterable<? extends AgentTrait<A, ?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return self();
        }

        public T addActions(AgentAction<A>... actions) {
            this.actions.addAll(asList(checkNotNull(actions)));
            return self();
        }

        public T addActions(Iterable<? extends AgentAction<A>> actions) {
            Iterables.addAll(this.actions, checkNotNull(actions));
            return self();
        }

        public T addProperties(AgentProperty<A, ?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return self();
        }

        public T addProperties(Iterable<? extends AgentProperty<A, ?>> properties) {
            Iterables.addAll(this.properties, checkNotNull(properties));
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            final Iterable<String> nameWithPossibleDuplicates = Iterables.transform(Iterables.concat(actions, properties, traits), new Function<AgentComponent, String>() {
                @Override
                public String apply(AgentComponent input) {
                    return input.getName();
                }
            });
            final String duplicate = Iterables.find(nameWithPossibleDuplicates, new Predicate<String>() {
                private final Set<String> nameSet = Sets.newHashSet();

                @Override
                public boolean apply(@Nullable String input) {
                    return ! nameSet.add(input);
                }
            }, null);
            checkState(duplicate == null, "You assigned the following name more than once to a component: " + duplicate);
        }
    }
}
