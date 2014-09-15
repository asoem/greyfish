package org.asoem.greyfish.impl.agent;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
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
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;

/**
 * The default implementation of {@code Basic2DAgent}.
 */
public final class DefaultBasic2DAgent extends AbstractSpatialAgent<Basic2DAgent, Basic2DSimulation, Point2D, Basic2DAgentContext>
        implements Basic2DAgent, Serializable {

    private final FunctionalList<AgentProperty<? super Basic2DAgentContext, ?>> properties;
    private final List<AgentAction<? super Basic2DAgentContext>> actions;
    private final FunctionalList<AgentTrait<? super Basic2DAgentContext, ?>> traits;
    private final ActionExecutionStrategy<Basic2DAgentContext> actionExecutionStrategy;
    private final FunctionalCollection<ACLMessage<Basic2DAgent>> inBox;
    @Nullable
    private Point2D projection;
    private Motion2D motion = ImmutableMotion2D.noMotion();
    @Nullable
    private BasicSimulationContext<Basic2DSimulation, Basic2DAgent> simulationContext;
    private final Basic2DAgentContext agentContext = new Basic2DAgentContext() {
        @Override
        public Basic2DAgent agent() {
            return DefaultBasic2DAgent.this;
        }

        @Override
        public Iterable<Basic2DAgent> getActiveAgents() {
            return getContext().get().getActiveAgents();
        }

        @Override
        public void receive(final ACLMessage<Basic2DAgent> message) {
            inBox.add(message);
        }

        @Override
        public Iterable<ACLMessage<Basic2DAgent>> getMessages(final MessageTemplate template) {
            return inBox.remove(template);
        }

        @Override
        public void sendMessage(final ACLMessage<Basic2DAgent> message) {
            getContext().get().getSimulation().deliverMessage(message);
        }
    };
    private final AgentType type;

    private DefaultBasic2DAgent(final Builder builder) {
        this.properties = ImmutableFunctionalList.copyOf(builder.properties);
        this.actions = ImmutableFunctionalList.copyOf(builder.actions);
        this.traits = ImmutableFunctionalList.copyOf(builder.traits);
        this.actionExecutionStrategy = new DefaultActionExecutionStrategy<Basic2DAgent, Basic2DAgentContext>(actions);
        this.inBox = new FunctionalFifoBuffer<>();
        this.type = builder.type;
    }

    @Override
    public AgentType getType() {
        return type;
    }

    @Override
    public void activate(final BasicSimulationContext<Basic2DSimulation, Basic2DAgent> context) {
        this.simulationContext = context;
    }

    @Override
    protected Basic2DAgent self() {
        return this;
    }

    public FunctionalList<AgentTrait<? super Basic2DAgentContext, ?>> getTraits() {
        return traits;
    }

    @Override
    protected Basic2DAgentContext agentContext() {
        return agentContext;
    }

    @Override
    public Motion2D getMotion() {
        return motion;
    }

    @Override
    public FunctionalList<AgentProperty<? super Basic2DAgentContext, ?>> getProperties() {
        return properties;
    }

    @Override
    public FunctionalList<AgentAction<? super Basic2DAgentContext>> getActions() {
        return ImmutableFunctionalList.copyOf(actions);
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
    public void setMotion(final Motion2D motion) {
        this.motion = checkNotNull(motion);
    }

    @Override
    public Optional<BasicSimulationContext<Basic2DSimulation, Basic2DAgent>> getContext() {
        return Optional.fromNullable(simulationContext);
    }

    @Override
    public <T> T getPropertyValue(final String traitName, final Class<T> valueType) {
        throw new UnsupportedOperationException("Not implemented");
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
    protected ActionExecutionStrategy<Basic2DAgentContext> getActionExecutionStrategy() {
        return actionExecutionStrategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Iterable<ACLMessage<Basic2DAgent>> getMessages(final MessageTemplate template) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public static final class Builder implements Serializable {
        private final List<AgentAction<? super Basic2DAgentContext>> actions = Lists.newArrayList();
        private final List<AgentProperty<? super Basic2DAgentContext, ?>> properties = Lists.newArrayList();
        private final List<AgentTrait<? super Basic2DAgentContext, ?>> traits = Lists.newArrayList();
        private AgentType type;

        protected Builder() {
        }

        protected Builder(final DefaultBasic2DAgent abstractAgent) {
            this.actions.addAll(abstractAgent.actions);
            this.properties.addAll(abstractAgent.properties);
            this.traits.addAll(abstractAgent.traits);
            this.type = abstractAgent.type;
        }

        public Builder addTraits(final AgentTrait<? super Basic2DAgentContext, ?>... traits) {
            this.traits.addAll(asList(checkNotNull(traits)));
            return this;
        }

        public Builder addTraits(final Iterable<? extends AgentTrait<? super Basic2DAgentContext, ?>> traits) {
            Iterables.addAll(this.traits, checkNotNull(traits));
            return this;
        }

        public Builder addAction(final AgentAction<? super AgentContext<Basic2DAgent>> action) {
            this.actions.add(checkNotNull(action));
            return this;
        }

        public Builder addActions(final AgentAction<? super AgentContext<Basic2DAgent>> action1, final AgentAction<? super AgentContext<Basic2DAgent>> action2) {
            addActions(ImmutableList.<AgentAction<? super AgentContext<Basic2DAgent>>>of(action1, action2));
            return this;
        }

        public Builder addActions(final AgentAction<? super AgentContext<Basic2DAgent>>... actions) {
            addActions(asList(checkNotNull(actions)));
            return this;
        }

        public Builder addActions(final Iterable<? extends AgentAction<? super AgentContext<Basic2DAgent>>> actions) {
            Iterables.addAll(this.actions, checkNotNull(actions));
            return this;
        }

        public Builder addProperties(final AgentProperty<? super Basic2DAgentContext, ?>... properties) {
            this.properties.addAll(asList(checkNotNull(properties)));
            return this;
        }

        public Builder addProperties(final Iterable<? extends AgentProperty<? super Basic2DAgentContext, ?>> properties) {
            Iterables.addAll(this.properties, checkNotNull(properties));
            return this;
        }

        public Builder setType(final AgentType type) {
            this.type = type;
            return this;
        }

        public DefaultBasic2DAgent build() throws IllegalStateException {
            final Iterable<String> nameWithPossibleDuplicates = Iterables.transform(Iterables.concat(actions, properties, traits), new Function<AgentComponent<?>, String>() {
                @Override
                public String apply(final AgentComponent<?> input) {
                    return input.getName();
                }
            });
            final String duplicate = Iterables.find(nameWithPossibleDuplicates, new Predicate<String>() {
                private final Set<String> nameSet = Sets.newHashSet();

                @Override
                public boolean apply(@Nullable final String input) {
                    return !nameSet.add(input);
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
