package org.asoem.greyfish.core.agent;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.ComponentContext;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.collect.FunctionalCollection;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.Functionals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class provides a skeletal implementation of the {@code Agent} interface.
 */
public abstract class AbstractAgent<A extends Agent<A, C>, C extends BasicSimulationContext<?, A>> implements Agent<A, C>, Descendant {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgent.class);

    private static <E extends AgentComponent> E findByName(final Iterable<E> elements, final String name) {
        final Optional<E> element = Functionals.tryFind(elements, new Predicate<AgentComponent>() {
            @Override
            public boolean apply(final AgentComponent agentAction) {
                return agentAction.getName().equals(name);
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

    @Override
    public final AgentAction<A> getAction(final String name) {
        return findByName(getActions(), name);
    }

    @Override
    public final AgentProperty<A, ?> getProperty(final String name) {
        return findByName(getProperties(), name);
    }

    @Override
    public abstract FunctionalList<AgentTrait<A, ?>> getTraits();

    @Override
    @Nullable
    public final AgentTrait<A, ?> getTrait(final String name) {
        return findByName(getTraits(), name);
    }

    @Override
    public final void receive(final ACLMessage<A> message) {
        LOGGER.debug("{} received a message: {}", this, message);
        getInBox().add(message);
    }

    @Override
    public final void receiveAll(final Iterable<? extends ACLMessage<A>> messages) {
        LOGGER.debug("{} received {} messages: {}", this, Iterables.size(messages), messages);
        Iterables.addAll(getInBox(), messages);
    }

    @Override
    public final Iterable<ACLMessage<A>> getMessages(final MessageTemplate template) {
        return getInBox().remove(template);
    }

    @Override
    public final boolean hasMessages(final MessageTemplate template) {
        return Iterables.any(getInBox(), template);
    }

    @Override
    public final void run() {
        final ActionExecutionStrategy<A> actionExecutionStrategy = getActionExecutionStrategy();
        final boolean executeSuccess = actionExecutionStrategy.executeNext(new ComponentContext<A, C>() {
            @Override
            public A agent() {
                return self();
            }

            @Override
            public C simulationContext() {
                return getContext().get();
            }
        });
        if (executeSuccess) {
            LOGGER.debug("{} executed {}", this, actionExecutionStrategy);
        }
    }

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

    @Override
    public final void activate(final C context) {
        checkNotNull(context);
        setSimulationContext(context);
        //logEvent(this, "activated", "");
        activated();
    }

    /**
     * A hook that gets called when this agent was activated with {@link Agent#activate(SimulationContext)}.
     */
    protected void activated() {
    }

    @Override
    public final void initialize() {
        setSimulationContext(null);
        getInBox().clear();
        setParents(ImmutableSet.<Integer>of());
        getActionExecutionStrategy().reset();
        for (final AgentNode node : children()) {
            node.initialize();
        }
    }

    @Override
    public abstract FunctionalList<AgentProperty<A, ?>> getProperties();

    @Override
    public abstract FunctionalList<AgentAction<A>> getActions();

    /*
    @Override
    public void reproduce(Initializer<? super A> initializer) {
        getSimulation().createAgent(getPrototypeGroup(), initializer);
    }
    */

    @Override
    public final void sendMessage(final ACLMessage<A> message) {
        getContext().get().deliverMessage(message);
    }

    @Override
    public final Iterable<AgentNode> children() {
        return Iterables.<AgentNode>concat(
                getProperties(),
                getActions(),
                getTraits()
        );
    }

    @Override
    public final AgentNode parent() {
        return null;
    }

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
    protected abstract ActionExecutionStrategy<A> getActionExecutionStrategy();

}
