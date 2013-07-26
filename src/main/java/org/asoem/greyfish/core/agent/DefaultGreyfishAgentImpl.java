package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.acl.FixedSizeMessageBox;
import org.asoem.greyfish.core.acl.MessageBox;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Point2D;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
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
public class DefaultGreyfishAgentImpl extends AbstractSpatialAgent<DefaultGreyfishAgent, DefaultGreyfishSimulation, Point2D> implements DefaultGreyfishAgent, Serializable {

    private final FunctionalList<AgentProperty<DefaultGreyfishAgent, ?>> properties;
    private final FunctionalList<AgentAction<DefaultGreyfishAgent>> actions;
    private final FunctionalList<AgentTrait<DefaultGreyfishAgent, ?>> traits;
    private final ActionExecutionStrategy actionExecutionStrategy;
    private final MessageBox<AgentMessage<DefaultGreyfishAgent>> inBox;
    private Population population;
    @Nullable
    private Point2D projection;
    private Motion2D motion = ImmutableMotion2D.noMotion();
    private SimulationContext<DefaultGreyfishSimulation, DefaultGreyfishAgent> simulationContext =
            PassiveSimulationContext.<DefaultGreyfishSimulation, DefaultGreyfishAgent>instance();
    private Set<Integer> parents = Collections.emptySet();

    @SuppressWarnings("unchecked") // casting a clone is safe
    private DefaultGreyfishAgentImpl(final DefaultGreyfishAgentImpl frozenAgent, final DeepCloner cloner) {
        cloner.addClone(frozenAgent, this);
        // share
        this.population = frozenAgent.population;
        // clone
        this.actions = ImmutableFunctionalList.copyOf(Iterables.transform(frozenAgent.actions, new Function<AgentAction<DefaultGreyfishAgent>, AgentAction<DefaultGreyfishAgent>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentAction<DefaultGreyfishAgent> apply(@Nullable final AgentAction<DefaultGreyfishAgent> agentAction) {
                return cloner.getClone(agentAction);
            }
        }));
        this.properties = ImmutableFunctionalList.copyOf(Iterables.transform(frozenAgent.properties, new Function<AgentProperty<DefaultGreyfishAgent, ?>, AgentProperty<DefaultGreyfishAgent, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentProperty<DefaultGreyfishAgent, ?> apply(@Nullable final AgentProperty<DefaultGreyfishAgent, ?> agentProperty) {
                return cloner.getClone(agentProperty);
            }
        }));
        this.traits = ImmutableFunctionalList.copyOf(Iterables.transform(frozenAgent.traits, new Function<AgentTrait<DefaultGreyfishAgent, ?>, AgentTrait<DefaultGreyfishAgent, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentTrait<DefaultGreyfishAgent, ?> apply(@Nullable final AgentTrait<DefaultGreyfishAgent, ?> agentTrait) {
                return cloner.getClone(agentTrait);
            }
        }));
        // reconstruct
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<AgentMessage<DefaultGreyfishAgent>>();
    }

    private DefaultGreyfishAgentImpl(final Builder builder) {
        this.properties = ImmutableFunctionalList.copyOf(builder.properties);
        for (final AgentProperty<DefaultGreyfishAgent, ?> property : builder.properties) {
            property.setAgent(this);
        }
        this.actions = ImmutableFunctionalList.copyOf(builder.actions);
        for (final AgentAction<DefaultGreyfishAgent> action : builder.actions) {
            action.setAgent(this);
        }
        this.traits = ImmutableFunctionalList.copyOf(builder.traits);
        for (final AgentTrait<DefaultGreyfishAgent, ?> trait : builder.traits) {
            trait.setAgent(this);
        }
        this.population = builder.population;
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FixedSizeMessageBox<AgentMessage<DefaultGreyfishAgent>>();
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(final Population population) {
        this.population = checkNotNull(population);
    }

    @Override
    protected DefaultGreyfishAgent self() {
        return this;
    }

    @Override
    public FunctionalList<AgentTrait<DefaultGreyfishAgent, ?>> getTraits() {
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
    public FunctionalList<AgentProperty<DefaultGreyfishAgent, ?>> getProperties() {
        return properties;
    }

    @Override
    public FunctionalList<AgentAction<DefaultGreyfishAgent>> getActions() {
        return actions;
    }

    @Nullable
    @Override
    public Point2D getProjection() {
        return projection;
    }

    @Override
    public void setProjection(@Nullable final Point2D projection) {
        this.projection = projection;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public void reproduce(final Chromosome chromosome) {
        simulation().createAgent(getPopulation(), getProjection(), chromosome);
    }

    @Override
    public void setMotion(final Motion2D motion) {
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
    protected MessageBox<AgentMessage<DefaultGreyfishAgent>> getInBox() {
        return inBox;
    }

    @Override
    protected void setSimulationContext(final SimulationContext<DefaultGreyfishSimulation, DefaultGreyfishAgent> simulationContext) {
        this.simulationContext = simulationContext;
    }

    @Override
    protected ActionExecutionStrategy getActionExecutionStrategy() {
        return actionExecutionStrategy;
    }

    @Override
    public void setParents(final Set<Integer> parents) {
        checkNotNull(parents);
        this.parents = parents;
    }

    @Override
    public DefaultGreyfishAgentImpl deepClone(final DeepCloner cloner) {
        return new DefaultGreyfishAgentImpl(this, cloner);
    }

    public static Builder builder(final Population population) {
        return new Builder(population);
    }

    public static class Builder implements Serializable {
        private final Population population;
        private final List<AgentAction<DefaultGreyfishAgent>> actions = Lists.newArrayList();
        private final List<AgentProperty<DefaultGreyfishAgent, ?>> properties = Lists.newArrayList();
        private final List<AgentTrait<DefaultGreyfishAgent, ?>> traits = Lists.newArrayList();

        protected Builder(final Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        protected Builder(final DefaultGreyfishAgentImpl abstractAgent) {
            this.population = abstractAgent.population;
            this.actions.addAll(abstractAgent.actions);
            this.properties.addAll(abstractAgent.properties);
            this.traits.addAll(abstractAgent.traits);
        }

        public Builder addTraits(final AgentTrait<DefaultGreyfishAgent, ?>... traits) {
            this.traits.addAll(asList(checkNotNull(traits)));
            return this;
        }

        public Builder addTraits(final Iterable<? extends AgentTrait<DefaultGreyfishAgent, ?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return this;
        }

        public Builder addAction(final AgentAction<DefaultGreyfishAgent> action) {
            this.actions.add(checkNotNull(action));
            return this;
        }

        public Builder addActions(final AgentAction<DefaultGreyfishAgent>... actions) {
            this.actions.addAll(asList(checkNotNull(actions)));
            return this;
        }

        public Builder addActions(final Iterable<? extends AgentAction<DefaultGreyfishAgent>> actions) {
            Iterables.addAll(this.actions, checkNotNull(actions));
            return this;
        }

        public Builder addProperties(final AgentProperty<DefaultGreyfishAgent, ?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return this;
        }

        public Builder addProperties(final Iterable<? extends AgentProperty<DefaultGreyfishAgent, ?>> properties) {
            Iterables.addAll(this.properties, checkNotNull(properties));
            return this;
        }

        public DefaultGreyfishAgentImpl build() throws IllegalStateException {
            final Iterable<String> nameWithPossibleDuplicates = Iterables.transform(Iterables.concat(actions, properties, traits), new Function<AgentComponent<DefaultGreyfishAgent>, String>() {
                @Override
                public String apply(final AgentComponent<DefaultGreyfishAgent> input) {
                    return input.getName();
                }
            });
            final String duplicate = Iterables.find(nameWithPossibleDuplicates, new Predicate<String>() {
                private final Set<String> nameSet = Sets.newHashSet();

                @Override
                public boolean apply(@Nullable final String input) {
                    return ! nameSet.add(input);
                }
            }, null);
            checkState(duplicate == null, "You assigned the following name more than once to a component: " + duplicate);

            return new DefaultGreyfishAgentImpl(this);
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
