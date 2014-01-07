package org.asoem.greyfish.impl.agent;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.impl.simulation.BasicSimulation;
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
public final class DefaultBasicAgent extends AbstractAgent<BasicAgent, BasicSimulationContext<BasicSimulation, BasicAgent>, BasicAgentContext<BasicAgent>>
        implements BasicAgent {
    private final PrototypeGroup prototypeGroup;
    private final FunctionalList<AgentAction<? super BasicAgentContext<BasicAgent>>> actions;
    private final FunctionalList<AgentProperty<? super BasicAgentContext<BasicAgent>, ?>> properties;
    private final FunctionalCollection<ACLMessage<BasicAgent>> inBox;
    private final transient ActionExecutionStrategy<BasicAgentContext<BasicAgent>> actionExecutionStrategy;

    @Nullable
    private BasicSimulationContext<BasicSimulation, BasicAgent> simulationContext;
    private final BasicAgentContext<BasicAgent> agentContext = new BasicAgentContext<BasicAgent>() {
        @Override
        public void addAgent(final BasicAgent agent) {
            getContext().get().getSimulation().enqueueAddition(agent);
        }

        @Override
        public void removeAgent(final BasicAgent agent) {
            getContext().get().getSimulation().enqueueRemoval(agent);
        }

        @Override
        public BasicAgent agent() {
            return DefaultBasicAgent.this;
        }

        @Override
        public Iterable<BasicAgent> getActiveAgents() {
            return getContext().get().getActiveAgents();
        }

        @Override
        public Iterable<BasicAgent> getAgents(final PrototypeGroup prototypeGroup) {
            return getContext().get().getAgents(prototypeGroup);
        }

        @Override
        public void receive(final ACLMessage<BasicAgent> message) {
            ask(message, Void.class);
        }

        @Override
        public Iterable<ACLMessage<BasicAgent>> getMessages(final MessageTemplate template) {
            return inBox.remove(template);
        }

        @Override
        public void sendMessage(final ACLMessage<BasicAgent> message) {
            getContext().get().getSimulation().deliverMessage(message);
        }
    };

    private DefaultBasicAgent(final Builder builder) {
        checkNotNull(builder);
        this.prototypeGroup = builder.prototypeGroup;
        this.actions = ImmutableFunctionalList.copyOf(builder.actions);
        this.properties = ImmutableFunctionalList.copyOf(builder.properties);
        this.inBox = builder.inBox;
        final ActionExecutionStrategyFactory<BasicAgentContext<BasicAgent>> actionExecutionStrategyFactory = builder.actionExecutionStrategyFactory;

        // TODO: write test for the following steps
        this.actionExecutionStrategy = actionExecutionStrategyFactory.create(actions);
        this.simulationContext = null;
    }

    @Override
    protected BasicAgent self() {
        return this;
    }

    @Override
    protected BasicAgentContext<BasicAgent> agentContext() {
        return agentContext;
    }

    public FunctionalList<AgentProperty<? super BasicAgentContext<BasicAgent>, ?>> getProperties() {
        return properties;
    }

    @Override
    public PrototypeGroup getPrototypeGroup() {
        return prototypeGroup;
    }

    @Override
    public void activate(final BasicSimulationContext<BasicSimulation, BasicAgent> context) {
        this.simulationContext = context;
    }

    protected FunctionalList<AgentAction<? super BasicAgentContext<BasicAgent>>> getActions() {
        return ImmutableFunctionalList.copyOf(actions);
    }

    @Override
    public Optional<BasicSimulationContext<BasicSimulation, BasicAgent>> getContext() {
        return Optional.fromNullable(simulationContext);
    }

    @Override
    public <T> T getPropertyValue(final String traitName, final Class<T> valueType) {
        final Optional<AgentProperty<? super BasicAgentContext<BasicAgent>, ?>> first = properties.findFirst(new Predicate<AgentProperty<? super BasicAgentContext<BasicAgent>, ?>>() {
            @Override
            public boolean apply(final AgentProperty<? super BasicAgentContext<BasicAgent>, ?> input) {
                return input.getName().equals(traitName);
            }
        });
        if (!first.isPresent()) {
            throw noSuchProperty(traitName);
        }
        final AgentProperty<? super BasicAgentContext<BasicAgent>, ?> agentProperty = first.get();
        return valueType.cast(agentProperty.value(agentContext()));
    }

    private RuntimeException noSuchProperty(final String traitName) {
        return new IllegalArgumentException("Agent has no property named '" + traitName + "'. " +
                "Available properties: [" + Joiner.on(", ").join(properties) + "]");
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
    protected ActionExecutionStrategy<BasicAgentContext<BasicAgent>> getActionExecutionStrategy() {
        return actionExecutionStrategy;
    }

    public static Builder builder(final PrototypeGroup prototypeGroup) {
        return new Builder(prototypeGroup);
    }

    public static final class Builder {
        private PrototypeGroup prototypeGroup;
        private final List<AgentAction<? super BasicAgentContext<BasicAgent>>> actions = Lists.newArrayList();
        private final List<AgentProperty<? super BasicAgentContext<BasicAgent>, ?>> properties = Lists.newArrayList();
        private final Set<Integer> parents = Sets.newHashSet();
        private FunctionalCollection<ACLMessage<BasicAgent>> inBox = FunctionalFifoBuffer.withCapacity(8);
        private ActionExecutionStrategyFactory<BasicAgentContext<BasicAgent>> actionExecutionStrategyFactory = new ActionExecutionStrategyFactory<BasicAgentContext<BasicAgent>>() {
            @Override
            public ActionExecutionStrategy<BasicAgentContext<BasicAgent>> create(final List<? extends AgentAction<? super BasicAgentContext<BasicAgent>>> actions) {
                return new DefaultActionExecutionStrategy<BasicAgent, BasicAgentContext<BasicAgent>>(actions);
            }
        };

        private Builder(final PrototypeGroup prototypeGroup) {
            this.prototypeGroup = checkNotNull(prototypeGroup);
        }

        public Builder addAction(final AgentAction<? super BasicAgentContext<BasicAgent>> action) {
            checkNotNull(action);
            actions.add(action);
            return this;
        }

        public Builder addAllActions(final AgentAction<? super BasicAgentContext<BasicAgent>> action1, final AgentAction<? super BasicAgentContext<BasicAgent>> action2) {
            checkNotNull(action1);
            checkNotNull(action2);
            addAllActions(ImmutableList.<AgentAction<? super BasicAgentContext<BasicAgent>>>of(action1, action2));
            return this;
        }

        public Builder addAllActions(final AgentAction<? super BasicAgentContext<BasicAgent>>... actions) {
            checkNotNull(actions);
            addAllActions(ImmutableList.copyOf(actions));
            return this;
        }

        public Builder addAllActions(final Iterable<? extends AgentAction<? super BasicAgentContext<BasicAgent>>> actions) {
            checkNotNull(actions);
            for (AgentAction<? super BasicAgentContext<BasicAgent>> action : actions) {
                addAction(action);
            }
            return this;
        }

        public Builder addProperty(final AgentProperty<? super BasicAgentContext<BasicAgent>, ?> property) {
            checkNotNull(property);
            properties.add(property);
            return this;
        }

        public Builder addAllProperties(final AgentProperty<? super BasicAgentContext<BasicAgent>, ?> property1, final AgentProperty<? super BasicAgentContext<BasicAgent>, ?> property2) {
            checkNotNull(property1);
            checkNotNull(property2);
            addAllProperties(ImmutableList.<AgentProperty<? super BasicAgentContext<BasicAgent>, ?>>of(property1, property2));
            return this;
        }

        public Builder addAllProperties(final Iterable<? extends AgentProperty<? super BasicAgentContext<BasicAgent>, ?>> properties) {
            checkNotNull(properties);
            for (AgentProperty<? super BasicAgentContext<BasicAgent>, ?> property : properties) {
                addProperty(property);
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
