/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import org.asoem.greyfish.impl.environment.BasicEnvironment;
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
public final class DefaultBasicAgent
        extends AbstractAgent<BasicAgent, BasicContext<BasicEnvironment, BasicAgent>, BasicAgentContext>
        implements BasicAgent {
    private final AgentType type;
    private final FunctionalList<AgentAction<? super BasicAgentContext>> actions;
    private final FunctionalList<AgentProperty<? super BasicAgentContext, ?>> properties;
    private final FunctionalCollection<ACLMessage<BasicAgent>> inBox;
    private final transient ActionScheduler<BasicAgentContext> actionScheduler;

    @Nullable
    private BasicContext<BasicEnvironment, BasicAgent> simulationContext;
    private final BasicAgentContext agentContext = new BasicAgentContext() {
        @Override
        public void addAgent(final BasicAgent agent) {
            getContext().get().getEnvironment().enqueueAddition(agent);
        }

        @Override
        public void removeAgent(final BasicAgent agent) {
            getContext().get().getEnvironment().enqueueRemoval(agent);
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
        public void receive(final ACLMessage<BasicAgent> message) {
            ask(message, Void.class);
        }

        @Override
        public Iterable<ACLMessage<BasicAgent>> getMessages(final MessageTemplate template) {
            return inBox.remove(template);
        }

        @Override
        public void sendMessage(final ACLMessage<BasicAgent> message) {
            getContext().get().getEnvironment().deliverMessage(message);
        }
    };

    private DefaultBasicAgent(final Builder builder) {
        checkNotNull(builder);
        this.actions = ImmutableFunctionalList.copyOf(builder.actions);
        this.properties = ImmutableFunctionalList.copyOf(builder.properties);
        this.inBox = builder.inBox;
        final ActionExecutionStrategyFactory<BasicAgentContext> actionExecutionStrategyFactory =
                builder.actionExecutionStrategyFactory;

        // TODO: write test for the following steps
        this.actionScheduler = actionExecutionStrategyFactory.create(actions);
        this.simulationContext = null;
        this.type = builder.type;
    }

    @Override
    protected BasicAgent self() {
        return this;
    }

    @Override
    protected BasicAgentContext agentContext() {
        return agentContext;
    }

    public FunctionalList<AgentProperty<? super BasicAgentContext, ?>> getProperties() {
        return properties;
    }

    @Override
    public AgentType getType() {
        return type;
    }

    @Override
    public void activate(final BasicContext<BasicEnvironment, BasicAgent> context) {
        this.simulationContext = context;
    }

    protected FunctionalList<AgentAction<? super BasicAgentContext>> getActions() {
        return ImmutableFunctionalList.copyOf(actions);
    }

    @Override
    public Optional<BasicContext<BasicEnvironment, BasicAgent>> getContext() {
        return Optional.fromNullable(simulationContext);
    }

    @Override
    public <T> T getPropertyValue(final String traitName, final Class<T> valueType) {
        final Optional<AgentProperty<? super BasicAgentContext, ?>> first =
                properties.findFirst(new Predicate<AgentProperty<? super BasicAgentContext, ?>>() {
                    @Override
                    public boolean apply(final AgentProperty<? super BasicAgentContext, ?> input) {
                        return input.getName().equals(traitName);
                    }
                });
        if (!first.isPresent()) {
            throw noSuchProperty(traitName);
        }
        final AgentProperty<? super BasicAgentContext, ?> agentProperty = first.get();
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
    protected void setSimulationContext(@Nullable final BasicContext<BasicEnvironment, BasicAgent> simulationContext) {
        this.simulationContext = simulationContext;
    }

    @Override
    protected ActionScheduler<BasicAgentContext> getActionScheduler() {
        return actionScheduler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<AgentAction<? super BasicAgentContext>> actions = Lists.newArrayList();
        private final List<AgentProperty<? super BasicAgentContext, ?>> properties = Lists.newArrayList();
        private final Set<Integer> parents = Sets.newHashSet();
        private FunctionalCollection<ACLMessage<BasicAgent>> inBox = FunctionalFifoBuffer.withCapacity(8);
        private ActionExecutionStrategyFactory<BasicAgentContext> actionExecutionStrategyFactory = new ActionExecutionStrategyFactory<BasicAgentContext>() {
            @Override
            public ActionScheduler<BasicAgentContext> create(final List<? extends AgentAction<? super BasicAgentContext>> actions) {
                return new DefaultActionScheduler<BasicAgent, BasicAgentContext>(actions);
            }
        };
        private AgentType type;

        private Builder() {
        }

        public Builder setType(final AgentType newType) {
            this.type = newType;
            return this;
        }

        public Builder addAction(final AgentAction<? super BasicAgentContext> action) {
            checkNotNull(action);
            actions.add(action);
            return this;
        }

        public Builder addAllActions(final AgentAction<? super BasicAgentContext> action1,
                                     final AgentAction<? super BasicAgentContext> action2) {
            checkNotNull(action1);
            checkNotNull(action2);
            addAllActions(ImmutableList.<AgentAction<? super BasicAgentContext>>of(action1, action2));
            return this;
        }

        public Builder addAllActions(final AgentAction<? super BasicAgentContext>... actions) {
            checkNotNull(actions);
            addAllActions(ImmutableList.copyOf(actions));
            return this;
        }

        public Builder addAllActions(final Iterable<? extends AgentAction<? super BasicAgentContext>> actions) {
            checkNotNull(actions);
            for (AgentAction<? super BasicAgentContext> action : actions) {
                addAction(action);
            }
            return this;
        }

        public Builder addProperty(final AgentProperty<? super BasicAgentContext, ?> property) {
            checkNotNull(property);
            properties.add(property);
            return this;
        }

        public Builder addAllProperties(final AgentProperty<? super BasicAgentContext, ?> property1,
                                        final AgentProperty<? super BasicAgentContext, ?> property2) {
            checkNotNull(property1);
            checkNotNull(property2);
            addAllProperties(ImmutableList.<AgentProperty<? super BasicAgentContext, ?>>of(property1, property2));
            return this;
        }

        public Builder addAllProperties(final AgentProperty<? super BasicAgentContext, ?>... properties) {
            checkNotNull(properties);
            addAllProperties(ImmutableList.<AgentProperty<? super BasicAgentContext, ?>>copyOf(properties));
            return this;
        }

        public Builder addAllProperties(
                final Iterable<? extends AgentProperty<? super BasicAgentContext, ?>> properties) {
            checkNotNull(properties);
            for (AgentProperty<? super BasicAgentContext, ?> property : properties) {
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
            checkState(type != null, "type is null");
            checkState(inBox != null, "inBox is null");
            checkState(actionExecutionStrategyFactory != null, "actionExecutionStrategyFactory is null");
            return new DefaultBasicAgent(this);
        }
    }
}
