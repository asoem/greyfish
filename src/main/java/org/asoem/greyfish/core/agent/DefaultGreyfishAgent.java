package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Point2D;

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
 * User: christoph
 * Date: 14.11.12
 * Time: 14:37
 */
public class DefaultGreyfishAgent extends AbstractAgent<DefaultGreyfishAgent, DefaultGreyfishSimulation, DefaultGreyfishSpace, Point2D> implements Serializable {

    private final SearchableList<AgentProperty<DefaultGreyfishAgent, ?>> properties;
    private final SearchableList<AgentAction<DefaultGreyfishAgent>> actions;
    private final SearchableList<AgentTrait<DefaultGreyfishAgent, ?>> traits;
    private final ActionExecutionStrategy actionExecutionStrategy;
    private final AgentMessageBox<DefaultGreyfishAgent> inBox;
    @Nullable
    private Population population;
    @Nullable
    private Point2D projection;
    private Motion2D motion = ImmutableMotion2D.noMotion();
    private SimulationContext<DefaultGreyfishSimulation,DefaultGreyfishAgent> simulationContext = PassiveSimulationContext.instance();
    private Set<Integer> parents = Collections.emptySet();

    protected DefaultGreyfishAgent(DefaultGreyfishAgent abstractAgent, final DeepCloner cloner) {
        cloner.addClone(abstractAgent, this);
        // share
        this.population = abstractAgent.population;
        // clone
        this.actions = AugmentedLists.copyOf(Iterables.transform(abstractAgent.actions, new Function<AgentAction<DefaultGreyfishAgent>, AgentAction<DefaultGreyfishAgent>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentAction<DefaultGreyfishAgent> apply(@Nullable AgentAction<DefaultGreyfishAgent> agentAction) {
                return (AgentAction<DefaultGreyfishAgent>) cloner.getClone(agentAction);
            }
        }));
        this.properties = AugmentedLists.copyOf(Iterables.transform(abstractAgent.properties, new Function<AgentProperty<DefaultGreyfishAgent, ?>, AgentProperty<DefaultGreyfishAgent, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentProperty<DefaultGreyfishAgent, ?> apply(@Nullable AgentProperty<DefaultGreyfishAgent, ?> agentProperty) {
                return (AgentProperty<DefaultGreyfishAgent, ?>) cloner.getClone(agentProperty);
            }
        }));
        this.traits = AugmentedLists.copyOf(Iterables.transform(abstractAgent.traits, new Function<AgentTrait<DefaultGreyfishAgent, ?>, AgentTrait<DefaultGreyfishAgent, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentTrait<DefaultGreyfishAgent, ?> apply(@Nullable AgentTrait<DefaultGreyfishAgent, ?> agentTrait) {
                return (AgentTrait<DefaultGreyfishAgent, ?>) cloner.getClone(agentTrait);
            }
        }));
        // reconstruct
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<DefaultGreyfishAgent>();
    }

    protected DefaultGreyfishAgent(Builder builder) {
        this.properties = AugmentedLists.copyOf(builder.properties);
        for (AgentProperty<DefaultGreyfishAgent, ?> property : builder.properties) {
            property.setAgent(this);
        }
        this.actions = AugmentedLists.copyOf(builder.actions);
        for (AgentAction<DefaultGreyfishAgent> action : builder.actions) {
            action.setAgent(self());
        }
        this.traits = AugmentedLists.copyOf(builder.traits);
        for (AgentTrait<DefaultGreyfishAgent, ?> trait : builder.traits) {
            trait.setAgent(self());
        }
        this.population = builder.population;
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<DefaultGreyfishAgent>();
    }

    @Override
    protected DefaultGreyfishAgent self() {
        return this;
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
    public SearchableList<AgentTrait<DefaultGreyfishAgent, ?>> getTraits() {
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
    public SearchableList<AgentProperty<DefaultGreyfishAgent, ?>> getProperties() {
        return properties;
    }

    @Override
    public SearchableList<AgentAction<DefaultGreyfishAgent>> getActions() {
        return actions;
    }

    @Nullable
    @Override
    public Point2D getProjection() {
        return projection;
    }

    @Override
    public void setProjection(@Nullable Point2D projection) {
        this.projection = projection;
    }

    @Override
    public boolean didCollide() {
        if (projection == null)
            throw new IllegalStateException("This agent has no projection");
        return projection.didCollide();
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
    protected SimulationContext<DefaultGreyfishSimulation, DefaultGreyfishAgent> getSimulationContext() {
        return simulationContext;
    }

    @Override
    protected AgentMessageBox<DefaultGreyfishAgent> getInBox() {
        return inBox;
    }

    @Override
    protected void setSimulationContext(SimulationContext<DefaultGreyfishSimulation, DefaultGreyfishAgent> simulationContext) {
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
    public DefaultGreyfishAgent deepClone(DeepCloner cloner) {
        return new DefaultGreyfishAgent(this, cloner);
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder implements org.asoem.greyfish.utils.base.Builder<DefaultGreyfishAgent>, Serializable {
        private final Population population;
        private final List<AgentAction<DefaultGreyfishAgent>> actions = Lists.newArrayList();
        private final List<AgentProperty<DefaultGreyfishAgent, ?>> properties = Lists.newArrayList();
        private final List<AgentTrait<DefaultGreyfishAgent, ?>> traits = Lists.newArrayList();

        protected Builder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        protected Builder(DefaultGreyfishAgent abstractAgent) {
            this.population = abstractAgent.population;
            this.actions.addAll(abstractAgent.actions);
            this.properties.addAll(abstractAgent.properties);
            this.traits.addAll(abstractAgent.traits);
        }

        public Builder addTraits(AgentTrait<DefaultGreyfishAgent, ?>... traits) {
            this.traits.addAll(asList(checkNotNull(traits)));
            return this;
        }

        public Builder addTraits(Iterable<? extends AgentTrait<DefaultGreyfishAgent, ?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return this;
        }

        public Builder addActions(AgentAction<DefaultGreyfishAgent>... actions) {
            this.actions.addAll(asList(checkNotNull(actions)));
            return this;
        }

        public Builder addActions(Iterable<? extends AgentAction<DefaultGreyfishAgent>> actions) {
            Iterables.addAll(this.actions, checkNotNull(actions));
            return this;
        }

        public Builder addProperties(AgentProperty<DefaultGreyfishAgent, ?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return this;
        }

        public Builder addProperties(Iterable<? extends AgentProperty<DefaultGreyfishAgent, ?>> properties) {
            Iterables.addAll(this.properties, checkNotNull(properties));
            return this;
        }

        @Override
        public DefaultGreyfishAgent build() throws IllegalStateException {
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

            return new DefaultGreyfishAgent(this);
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
