package org.asoem.greyfish.impl.agent;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.FunctionalCollection;
import org.asoem.greyfish.utils.collect.FunctionalFifoBuffer;
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
 * The default implementation of {@code Basic2DAgent}.
 */
public final class DefaultBasic2DAgent extends AbstractSpatialAgent<Basic2DAgent, Basic2DSimulation, Point2D>
        implements Basic2DAgent, Serializable {

    private final FunctionalList<AgentProperty<Basic2DAgent, ?>> properties;
    private final FunctionalList<AgentAction<Basic2DAgent>> actions;
    private final FunctionalList<AgentTrait<Basic2DAgent, ?>> traits;
    private final ActionExecutionStrategy actionExecutionStrategy;
    private final FunctionalCollection<ACLMessage<Basic2DAgent>> inBox;
    private PrototypeGroup prototypeGroup;
    @Nullable
    private Point2D projection;
    private Motion2D motion = ImmutableMotion2D.noMotion();
    @Nullable
    private BasicSimulationContext<Basic2DSimulation, Basic2DAgent> simulationContext;
    private Set<Integer> parents = Collections.emptySet();

    @SuppressWarnings("unchecked") // casting a clone is safe
    private DefaultBasic2DAgent(final DefaultBasic2DAgent frozenAgent, final DeepCloner cloner) {
        cloner.addClone(frozenAgent, this);
        // share
        this.prototypeGroup = frozenAgent.prototypeGroup;
        // clone
        this.actions = ImmutableFunctionalList.copyOf(Iterables.transform(frozenAgent.actions, new Function<AgentAction<Basic2DAgent>, AgentAction<Basic2DAgent>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentAction<Basic2DAgent> apply(@Nullable final AgentAction<Basic2DAgent> agentAction) {
                return cloner.getClone(agentAction);
            }
        }));
        this.properties = ImmutableFunctionalList.copyOf(Iterables.transform(frozenAgent.properties, new Function<AgentProperty<Basic2DAgent, ?>, AgentProperty<Basic2DAgent, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentProperty<Basic2DAgent, ?> apply(@Nullable final AgentProperty<Basic2DAgent, ?> agentProperty) {
                return cloner.getClone(agentProperty);
            }
        }));
        this.traits = ImmutableFunctionalList.copyOf(Iterables.transform(frozenAgent.traits, new Function<AgentTrait<Basic2DAgent, ?>, AgentTrait<Basic2DAgent, ?>>() {
            @SuppressWarnings("unchecked")
            @Override
            public AgentTrait<Basic2DAgent, ?> apply(@Nullable final AgentTrait<Basic2DAgent, ?> agentTrait) {
                return cloner.getClone(agentTrait);
            }
        }));
        // reconstruct
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FunctionalFifoBuffer<ACLMessage<Basic2DAgent>>();
    }

    private DefaultBasic2DAgent(final Builder builder) {
        this.properties = ImmutableFunctionalList.copyOf(builder.properties);
        for (final AgentProperty<Basic2DAgent, ?> property : builder.properties) {
            property.setAgent(this);
        }
        this.actions = ImmutableFunctionalList.copyOf(builder.actions);
        for (final AgentAction<Basic2DAgent> action : builder.actions) {
            action.setAgent(this);
        }
        this.traits = ImmutableFunctionalList.copyOf(builder.traits);
        for (final AgentTrait<Basic2DAgent, ?> trait : builder.traits) {
            trait.setAgent(this);
        }
        this.prototypeGroup = builder.prototypeGroup;
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy(actions);
        this.inBox = new FunctionalFifoBuffer<ACLMessage<Basic2DAgent>>();
    }

    @Override
    public PrototypeGroup getPrototypeGroup() {
        return prototypeGroup;
    }

    @Override
    protected Basic2DAgent self() {
        return this;
    }

    @Override
    public FunctionalList<AgentTrait<Basic2DAgent, ?>> getTraits() {
        return traits;
    }

    @Override
    public Motion2D getMotion() {
        return motion;
    }

    @Override
    public FunctionalList<AgentProperty<Basic2DAgent, ?>> getProperties() {
        return properties;
    }

    @Override
    public FunctionalList<AgentAction<Basic2DAgent>> getActions() {
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
        final Basic2DAgent clone = CycleCloner.clone(this);
        chromosome.updateAgent(clone);
        getContext().get().getSimulation().enqueueAddition(clone, getProjection());
    }

    @Override
    public void setMotion(final Motion2D motion) {
        this.motion = checkNotNull(motion);
    }

    @Override
    public String toString() {
        return "Agent[" + getPrototypeGroup() + ']' + "#"
                + (getContext().isPresent() ? getContext().get().getAgentId() : "null")
                + "@" + (getContext().isPresent() ? getContext().get().getSimulationStep() : "null");
    }

    @Override
    public Optional<BasicSimulationContext<Basic2DSimulation, Basic2DAgent>> getContext() {
        return Optional.fromNullable(simulationContext);
    }

    @Override
    protected FunctionalCollection<ACLMessage<Basic2DAgent>> getInBox() {
        return inBox;
    }

    @Override
    protected void setSimulationContext(@Nullable final BasicSimulationContext<Basic2DSimulation, Basic2DAgent> simulationContext) {
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
    public DefaultBasic2DAgent deepClone(final DeepCloner cloner) {
        return new DefaultBasic2DAgent(this, cloner);
    }

    public static Builder builder(final PrototypeGroup prototypeGroup) {
        return new Builder(prototypeGroup);
    }

    public static final class Builder implements Serializable {
        private final PrototypeGroup prototypeGroup;
        private final List<AgentAction<Basic2DAgent>> actions = Lists.newArrayList();
        private final List<AgentProperty<Basic2DAgent, ?>> properties = Lists.newArrayList();
        private final List<AgentTrait<Basic2DAgent, ?>> traits = Lists.newArrayList();

        protected Builder(final PrototypeGroup prototypeGroup) {
            this.prototypeGroup = checkNotNull(prototypeGroup, "PrototypeGroup must not be null");
        }

        protected Builder(final DefaultBasic2DAgent abstractAgent) {
            this.prototypeGroup = abstractAgent.prototypeGroup;
            this.actions.addAll(abstractAgent.actions);
            this.properties.addAll(abstractAgent.properties);
            this.traits.addAll(abstractAgent.traits);
        }

        public Builder addTraits(final AgentTrait<Basic2DAgent, ?>... traits) {
            this.traits.addAll(asList(checkNotNull(traits)));
            return this;
        }

        public Builder addTraits(final Iterable<? extends AgentTrait<Basic2DAgent, ?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return this;
        }

        public Builder addAction(final AgentAction<Basic2DAgent> action) {
            this.actions.add(checkNotNull(action));
            return this;
        }

        public Builder addActions(final AgentAction<Basic2DAgent> action1, final AgentAction<Basic2DAgent> action2) {
            addActions(ImmutableList.of(action1, action2));
            return this;
        }

        public Builder addActions(final AgentAction<Basic2DAgent>... actions) {
            addActions(asList(checkNotNull(actions)));
            return this;
        }

        public Builder addActions(final Iterable<? extends AgentAction<Basic2DAgent>> actions) {
            Iterables.addAll(this.actions, checkNotNull(actions));
            return this;
        }

        public Builder addProperties(final AgentProperty<Basic2DAgent, ?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return this;
        }

        public Builder addProperties(final Iterable<? extends AgentProperty<Basic2DAgent, ?>> properties) {
            Iterables.addAll(this.properties, checkNotNull(properties));
            return this;
        }

        public DefaultBasic2DAgent build() throws IllegalStateException {
            final Iterable<String> nameWithPossibleDuplicates = Iterables.transform(Iterables.concat(actions, properties, traits), new Function<AgentComponent<Basic2DAgent>, String>() {
                @Override
                public String apply(final AgentComponent<Basic2DAgent> input) {
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

            return new DefaultBasic2DAgent(this);
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
