package org.asoem.greyfish.impl.agent;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.impl.simulation.BasicSimulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.FunctionalCollection;
import org.asoem.greyfish.utils.collect.FunctionalFifoBuffer;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The default implementation of {@code BasicAgent}.
 */
public final class DefaultBasicAgent extends AbstractAgent<BasicAgent, BasicSimulationContext<BasicSimulation, BasicAgent>>
        implements BasicAgent {
    private final PrototypeGroup prototypeGroup;
    private final FunctionalList<AgentAction<BasicAgent>> actions;
    private final FunctionalList<AgentTrait<BasicAgent, ?>> traits;
    private final FunctionalList<AgentProperty<BasicAgent, ?>> properties;
    private final FunctionalCollection<ACLMessage<BasicAgent>> inBox;
    private final ActionExecutionStrategyFactory actionExecutionStrategyFactory;
    private final transient ActionExecutionStrategy actionExecutionStrategy;

    private Set<Integer> parents;
    @Nullable
    private BasicSimulationContext<BasicSimulation, BasicAgent> simulationContext;

    private DefaultBasicAgent(final DefaultBasicAgent original, final DeepCloner cloner) {
        checkNotNull(original);
        checkNotNull(cloner);
        cloner.addClone(original, this);
        this.prototypeGroup = original.prototypeGroup;
        this.actions = ImmutableFunctionalList.copyOf(Iterables.transform(original.actions, new Function<AgentAction<BasicAgent>, AgentAction<BasicAgent>>() {
            @Nullable
            @Override
            public AgentAction<BasicAgent> apply(@Nullable final AgentAction<BasicAgent> input) {
                return cloner.getClone(input);
            }
        }));
        this.traits = ImmutableFunctionalList.copyOf(Iterables.transform(original.traits, new Function<AgentTrait<BasicAgent, ?>, AgentTrait<BasicAgent, ?>>() {
            @Nullable
            @Override
            public AgentTrait<BasicAgent, ?> apply(@Nullable final AgentTrait<BasicAgent, ?> input) {
                return cloner.getClone(input);
            }
        }));
        this.properties = ImmutableFunctionalList.copyOf(Iterables.transform(original.properties, new Function<AgentProperty<BasicAgent, ?>, AgentProperty<BasicAgent, ?>>() {
            @Nullable
            @Override
            public AgentProperty<BasicAgent, ?> apply(@Nullable final AgentProperty<BasicAgent, ?> input) {
                return cloner.getClone(input);
            }
        }));
        this.parents = original.parents;
        this.simulationContext = original.simulationContext;
        this.inBox = original.inBox;
        this.actionExecutionStrategyFactory = original.actionExecutionStrategyFactory;
        this.actionExecutionStrategy = actionExecutionStrategyFactory.create(actions);
    }

    private DefaultBasicAgent(final Builder builder) {
        checkNotNull(builder);
        this.prototypeGroup = builder.prototypeGroup;
        this.actions = ImmutableFunctionalList.<AgentAction<BasicAgent>>copyOf(builder.actions);
        this.traits = ImmutableFunctionalList.<AgentTrait<BasicAgent, ?>>copyOf(builder.traits);
        this.properties = ImmutableFunctionalList.<AgentProperty<BasicAgent, ?>>copyOf(builder.properties);
        this.parents = builder.parents;
        this.inBox = builder.inBox;
        this.actionExecutionStrategyFactory = builder.actionExecutionStrategyFactory;

        // TODO: write test for the following steps
        this.actionExecutionStrategy = actionExecutionStrategyFactory.create(actions);
        this.simulationContext = null;
        for (AgentAction<BasicAgent> action : actions) {
            action.setAgent(this);
        }
        for (AgentTrait<BasicAgent, ?> trait : traits) {
            trait.setAgent(this);
        }
        for (AgentProperty<BasicAgent, ?> property : properties) {
            property.setAgent(this);
        }
    }

    @Override
    protected BasicAgent self() {
        return this;
    }

    @Override
    public FunctionalList<AgentTrait<BasicAgent, ?>> getTraits() {
        return traits;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public void setParents(final Set<Integer> parents) {
        this.parents = checkNotNull(parents);
    }

    @Override
    public FunctionalList<AgentProperty<BasicAgent, ?>> getProperties() {
        return properties;
    }

    @Override
    public PrototypeGroup getPrototypeGroup() {
        return prototypeGroup;
    }

    @Override
    public FunctionalList<AgentAction<BasicAgent>> getActions() {
        return actions;
    }

    @Override
    public Optional<BasicSimulationContext<BasicSimulation, BasicAgent>> getContext() {
        return Optional.fromNullable(simulationContext);
    }

    @Override
    protected FunctionalCollection<ACLMessage<BasicAgent>> getInBox() {
        return inBox;
    }

    @Override
    protected void setSimulationContext(@Nullable final BasicSimulationContext<BasicSimulation, BasicAgent> simulationContext) {
        this.simulationContext = simulationContext;
    }

    @Override
    protected ActionExecutionStrategy getActionExecutionStrategy() {
        return actionExecutionStrategy;
    }

    @Override
    public DeepCloneable deepClone(final DeepCloner cloner) {
        return new DefaultBasicAgent(this, cloner);
    }

    public static Builder builder(final PrototypeGroup prototypeGroup) {
        return new Builder(prototypeGroup);
    }

    public static final class Builder {
        private PrototypeGroup prototypeGroup;
        private final List<AgentAction<BasicAgent>> actions = Lists.newArrayList();
        private final List<AgentTrait<BasicAgent, ?>> traits = Lists.newArrayList();
        private final List<AgentProperty<BasicAgent, ?>> properties = Lists.newArrayList();
        private final Set<Integer> parents = Sets.newHashSet();
        private FunctionalCollection<ACLMessage<BasicAgent>> inBox = FunctionalFifoBuffer.withCapacity(8);
        private ActionExecutionStrategyFactory actionExecutionStrategyFactory = DefaultActionExecutionStrategyFactory.INSTANCE;

        private Builder(final PrototypeGroup prototypeGroup) {
            this.prototypeGroup = checkNotNull(prototypeGroup);
        }

        public Builder addAction(final AgentAction<BasicAgent> action) {
            checkNotNull(action);
            actions.add(action);
            return this;
        }

        public Builder addAllActions(final AgentAction<BasicAgent> action1, final AgentAction<BasicAgent> action2) {
            checkNotNull(action1);
            checkNotNull(action2);
            addAllActions(ImmutableList.of(action1, action2));
            return this;
        }

        public Builder addAllActions(final AgentAction<BasicAgent>... actions) {
            checkNotNull(actions);
            addAllActions(ImmutableList.copyOf(actions));
            return this;
        }

        public Builder addAllActions(final Iterable<? extends AgentAction<BasicAgent>> actions) {
            checkNotNull(actions);
            for (AgentAction<BasicAgent> action : actions) {
                addAction(action);
            }
            return this;
        }

        public Builder addProperty(final AgentProperty<BasicAgent, ?> property) {
            checkNotNull(property);
            properties.add(property);
            return this;
        }

        public Builder addAllProperties(final AgentProperty<BasicAgent, ?> property1, final AgentProperty<BasicAgent, ?> property2) {
            checkNotNull(property1);
            checkNotNull(property2);
            addAllProperties(ImmutableList.of(property1, property2));
            return this;
        }

        public Builder addAllProperties(final Iterable<? extends AgentProperty<BasicAgent, ?>> properties) {
            checkNotNull(properties);
            for (AgentProperty<BasicAgent, ?> property : properties) {
                addProperty(property);
            }
            return this;
        }

        public Builder addTrait(final AgentTrait<BasicAgent, ?> trait) {
            checkNotNull(trait);
            traits.add(trait);
            return this;
        }

        public Builder addAllTraits(final AgentTrait<BasicAgent, ?> trait1, final AgentTrait<BasicAgent, ?> trait2) {
            checkNotNull(trait1);
            checkNotNull(trait2);
            addAllTraits(ImmutableList.of(trait1, trait2));
            return this;
        }

        public Builder addAllTraits(final AgentTrait<BasicAgent, ?>... traits) {
            checkNotNull(traits);
            addAllTraits(ImmutableList.copyOf(traits));
            return this;
        }

        public Builder addAllTraits(final Iterable<AgentTrait<BasicAgent, ?>> traits) {
            checkNotNull(traits);
            for (AgentTrait<BasicAgent, ?> trait : traits) {
                addTrait(trait);
            }
            return this;
        }

        public Builder parents(final Set<Integer> parents) {
            checkNotNull(parents);
            for (Integer parent : parents) {
                parents.add(parent);
            }
            return this;
        }

        public DefaultBasicAgent build() {
            checkState(prototypeGroup != null);
            checkState(inBox != null);
            checkNotNull(actionExecutionStrategyFactory != null);
            return new DefaultBasicAgent(this);
        }
    }
}
