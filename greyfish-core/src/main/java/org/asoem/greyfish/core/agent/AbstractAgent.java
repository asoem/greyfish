package org.asoem.greyfish.core.agent;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.utils.collect.FunctionalCollection;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.ImmutableMapBuilder;
import org.asoem.greyfish.utils.collect.Searchable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

/**
 * This class provides a skeletal implementation of the {@code Agent} interface.
 */
public abstract class AbstractAgent<A extends Agent<C>, C extends BasicSimulationContext<?, A>, AC extends AgentContext<A>> implements Agent<C> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgent.class);

    private static <E extends AgentComponent<?>> E findByName(final Searchable<E> elements, final String name) {
        final Optional<E> element = elements.findFirst(new Predicate<E>() {
            @Override
            public boolean apply(final E input) {
                return Objects.equal(name, input.getName());
            }
        });

        if (!element.isPresent()) {
            throw new NoSuchElementException("Could not find component with name equal to " + name);
        }

        return element.get();
    }

    /**
     * The class implementing this {@code AbstractAgent} which defines the actual type of {@code A} should return {@code
     * this} here.
     *
     * @return the agent instance
     */
    protected abstract A self();

    @Nullable
    public final AgentProperty<? super AC, ?> getProperty(final String name) {
        return findByName(getProperties(), name);
    }

    public final void receive(final ACLMessage<A> message) {
        LOGGER.debug("{} received a message: {}", this, message);
        getInBox().add(message);
    }

    @Override
    public final void run() {
        final ActionExecutionStrategy<AC> actionExecutionStrategy = getActionExecutionStrategy();
        final boolean executeSuccess = actionExecutionStrategy.executeNext(agentContext());
        if (executeSuccess) {
            LOGGER.debug("{} executed {}", this, actionExecutionStrategy);
        }
    }

    protected abstract AC agentContext();

    @Override
    public final void deactivate() {
        initialize();
        deactivated();
    }

    /**
     * A hook method which gets called when this agent gets deactivated using {@link
     * org.asoem.greyfish.core.agent.Agent#deactivate()}
     */
    protected void deactivated() {
    }

    @Override
    public final boolean isActive() {
        return getContext().isPresent();
    }

    public final <T> T ask(final ComponentMessage message, final Class<T> replyType) {
        final String traitName = message.componentName();
        final AgentProperty<? super AC, ?> trait = getProperty(traitName);
        if (trait == null) {
            throw new UnsupportedOperationException("Could not find trait with name = " + traitName);
        }
        return replyType.cast(trait.tell(agentContext(), message, replyType));
    }

    @Override
    public <T> T ask(final Object message, final Class<T> replyType) {
        if (message instanceof ComponentMessage) {
            final ComponentMessage request = (ComponentMessage) message;
            return ask(request, replyType);
        } else if (message instanceof ACLMessage) {
            receive((ACLMessage<A>) message);
            return replyType.cast(null);
        } else if (message instanceof RequestAllTraitValues) {
            FunctionalList<AgentProperty<? super AC, ?>> properties = getProperties();
            final ImmutableMapBuilder<String, Object> builder = ImmutableMapBuilder.newInstance();
            for (AgentProperty<? super AC, ?> property : properties) {
                String name = property.getName();
                Object value = property.value(agentContext());
                builder.put(name, value);
            }
            return replyType.cast(builder.build());
        }

        throw new IllegalArgumentException();
    }

    /*
    @Override
    public final void activate(final C context) {
        checkNotNull(context);
        setSimulationContext(context);
        //logEvent(this, "activated", "");
        activated();
    }
    */

    @Override
    public final void initialize() {
        setSimulationContext(null);
        getInBox().clear();
        //setParents(ImmutableSet.<Integer>of());
        getActionExecutionStrategy().reset();
        for (final AgentNode node : children()) {
            node.initialize();
        }
    }

    /*
    @Override
    public void reproduce(Initializer<? super A> initializer) {
        getSimulation().createAgent(getPrototypeGroup(), initializer);
    }
    */

    @Override
    public final Iterable<AgentNode> children() {
        return Iterables.<AgentNode>concat(
                getProperties(),
                getActions()
        );
    }

    protected abstract FunctionalList<AgentProperty<? super AC, ?>> getProperties();

    protected abstract FunctionalList<AgentAction<? super AC>> getActions();

    /**
     * Set the getSimulation context to {@code simulationContext}.
     *
     * @param simulationContext the new getSimulation context
     */
    protected abstract void setSimulationContext(@Nullable C simulationContext);

    /**
     * Get the message box.
     *
     * @return the message box
     */
    protected abstract FunctionalCollection<ACLMessage<A>> getInBox();

    /**
     * Get the action execution strategy.
     *
     * @return the action execution strategy
     */
    protected abstract ActionExecutionStrategy<AC> getActionExecutionStrategy();

}
