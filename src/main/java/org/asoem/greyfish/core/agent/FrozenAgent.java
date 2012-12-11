package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.SpatialSimulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;


/**
 * An {@code FrozenAgent} is an implementation of an {@link Agent} which guarantees no structural changes after construction.
 * This can prevent bugs but also allows for structure dependent performance optimizations.
 * This means, you cannot add or remove any {@link AgentComponent} to this {@code Agent}.
 * If you try to, it will throw an {@link UnsupportedOperationException}.
 * However, no guarantees can be made about the {@code AgentComponent}s themselves,
 * but should generally act according to the frozen state of their parent component.
 */
public class FrozenAgent<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation<A, Z>, P extends Object2D, Z extends Space2D<A, P>> extends AbstractAgent<A, S, P> implements SpatialAgent<A, S, P>, Serializable {

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
    private SimulationContext<S,A> simulationContext = PassiveSimulationContext.<S, A>instance();
    private Set<Integer> parents = Collections.emptySet();
    private final A self;

    @SuppressWarnings("unchecked") // casting a clone is safe
    private FrozenAgent(FrozenAgent<A, S, P, Z> frozenAgent, final DeepCloner cloner) {
        cloner.addClone(frozenAgent, this);
        // share
        this.population = frozenAgent.population;
        // clone
        this.actions = AugmentedLists.copyOf(Iterables.transform(frozenAgent.actions, new Function<AgentAction<A>, AgentAction<A>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentAction<A> apply(@Nullable AgentAction<A> agentAction) {
                return (AgentAction<A>) cloner.getClone(agentAction);
            }
        }));
        this.properties = AugmentedLists.copyOf(Iterables.transform(frozenAgent.properties, new Function<AgentProperty<A, ?>, AgentProperty<A, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentProperty<A, ?> apply(@Nullable AgentProperty<A, ?> agentProperty) {
                return (AgentProperty<A, ?>) cloner.getClone(agentProperty);
            }
        }));
        this.traits = AugmentedLists.copyOf(Iterables.transform(frozenAgent.traits, new Function<AgentTrait<A, ?>, AgentTrait<A, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentTrait<A, ?> apply(@Nullable AgentTrait<A, ?> agentTrait) {
                return (AgentTrait<A, ?>) cloner.getClone(agentTrait);
            }
        }));
        // reconstruct
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<A>();
        this.self = (A) cloner.getClone(frozenAgent.self);
    }

    private FrozenAgent(Builder<A, S, P, Z> builder) {
        this.properties = AugmentedLists.copyOf(builder.properties);
        this.self = builder.self;
        for (AgentProperty<A, ?> property : builder.properties) {
            property.setAgent(self());
        }
        this.actions = AugmentedLists.copyOf(builder.actions);
        for (AgentAction<A> action : builder.actions) {
            action.setAgent(self());
        }
        this.traits = AugmentedLists.copyOf(builder.traits);
        for (AgentTrait<A, ?> trait : builder.traits) {
            trait.setAgent(self());
        }
        this.population = builder.population;
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<A>();
    }

    @Override
    protected A self() {
        return self;
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

    @Override
    public FrozenAgent<A, S, P, Z> deepClone(DeepCloner cloner) {
        return new FrozenAgent<A, S, P, Z>(this, cloner);
    }

    private Object writeReplace() {
        return new Builder<A, S, P, Z>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends SpatialAgent<A, S, P>, S extends SpatialSimulation<A, Z>, P extends Object2D, Z extends Space2D<A, P>> Builder<A, S, P, Z> builder(Population population) {
        return new Builder<A, S, P, Z>(population);
    }

    @Override
    public double distance(A agent, double degrees) {
        return simulation().distance(agent, degrees);
    }

    @Override
    public Iterable<A> findNeighbours(double radius) {
        return simulation().findNeighbours(self, radius);
    }

    public static class Builder<A extends SpatialAgent<A, S, P>, S extends SpatialSimulation<A, Z>, P extends Object2D, Z extends Space2D<A, P>> implements org.asoem.greyfish.utils.base.Builder<FrozenAgent<A, S, P, Z>>, Serializable {
        private final Population population;
        private final List<AgentAction<A>> actions = Lists.newArrayList();
        private final List<AgentProperty<A, ?>> properties = Lists.newArrayList();
        private final List<AgentTrait<A, ?>> traits = Lists.newArrayList();
        private A self;

        protected Builder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        protected Builder(FrozenAgent<A, S, P, Z> abstractAgent) {
            this.population = abstractAgent.population;
            this.actions.addAll(abstractAgent.actions);
            this.properties.addAll(abstractAgent.properties);
            this.traits.addAll(abstractAgent.traits);
        }

        public Builder<A, S, P, Z> addTraits(AgentTrait<A, ?>... traits) {
            this.traits.addAll(asList(checkNotNull(traits)));
            return this;
        }

        public Builder<A, S, P, Z> addTraits(Iterable<? extends AgentTrait<A, ?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return this;
        }

        public Builder<A, S, P, Z> addAction(AgentAction<A> action) {
            this.actions.add(checkNotNull(action));
            return this;
        }

        public Builder<A, S, P, Z> addActions(AgentAction<A>... actions) {
            this.actions.addAll(asList(checkNotNull(actions)));
            return this;
        }

        public Builder<A, S, P, Z> addActions(Iterable<? extends AgentAction<A>> actions) {
            Iterables.addAll(this.actions, checkNotNull(actions));
            return this;
        }

        public Builder<A, S, P, Z> addProperties(AgentProperty<A, ?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return this;
        }

        public Builder<A, S, P, Z> addProperties(Iterable<? extends AgentProperty<A, ?>> properties) {
            Iterables.addAll(this.properties, checkNotNull(properties));
            return this;
        }


        public Builder<A, S, P, Z> self(A agent) {
            self = agent;
            return this;
        }

        @Override
        public FrozenAgent<A, S, P, Z> build() throws IllegalStateException {
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

            return new FrozenAgent<A, S, P, Z>(this);
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
}
