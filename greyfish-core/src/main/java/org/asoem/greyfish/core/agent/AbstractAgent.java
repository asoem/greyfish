package org.asoem.greyfish.core.agent;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.collect.FunctionalCollection;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * This class provides a skeletal implementation of the {@code Agent} interface.
 */
public abstract class AbstractAgent<A extends Agent<A, S>, S extends DiscreteTimeSimulation<A>> implements Agent<A, S> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgent.class);

    private static <E extends AgentComponent> E findByName(final FunctionalList<E> functionalList, final String name) {
        return functionalList.findFirst(new Predicate<AgentComponent>() {
            @Override
            public boolean apply(final AgentComponent agentAction) {
                return agentAction.getName().equals(name);
            }
        }).get();
    }

    /**
     * The class implementing this {@code AbstractAgent} which defines the actual type of {@code A} should return {@code this} here.
     * @return the agent instance
     */
    protected abstract A self();

    @Override
    public final boolean hasPopulation(@Nullable final Population population) {
        return Objects.equal(getPopulation(), population);
    }

    private <E extends AgentComponent<A>> boolean addComponent(final FunctionalList<E> list, final E element) {
        if (list.add(element)) {
            element.setAgent(self());
            return true;
        }
        return false;
    }

    private <E extends AgentComponent<A>> boolean removeComponent(final FunctionalList<? extends E> list, final E element) {
        if (list.remove(element)) {
            element.setAgent(self());
            return true;
        }
        return false;
    }

    private void clearComponentList(final FunctionalList<? extends AgentComponent<A>> list) {
        final List<AgentComponent<A>> temp = ImmutableList.copyOf(list);
        list.clear();
        for (final AgentComponent<A> component : temp)
            component.setAgent(null);
    }

    @Override
    public final boolean addAction(final AgentAction<A> action) {
        return addComponent(getActions(), action);
    }

    @Override
    public final boolean removeAction(final AgentAction<A> action) {
        return removeComponent(getActions(), action);
    }

    @Override
    public final void removeAllActions() {
        clearComponentList(getActions());
    }

    @Override
    public final AgentAction<A> getAction(final String name) {
        return findByName(getActions(), name);
    }

    @Override
    public final boolean addProperty(final AgentProperty<A, ?> property) {
        return addComponent(getProperties(), property);
    }

    @Override
    public final boolean removeProperty(final AgentProperty<A, ?> property) {
        return removeComponent(getProperties(), property);
    }

    @Override
    public final void removeAllProperties() {
        clearComponentList(getProperties());
    }

    @Override
    public final AgentProperty<A, ?> getProperty(final String name) {
        return findByName(getProperties(), name);
    }

    @Override
    public final AgentProperty<A, ?> findProperty(final Predicate<? super AgentProperty<A, ?>> predicate) {
        return getProperties().findFirst(predicate).get();
    }

    @Override
    public final boolean addTrait(final AgentTrait<A, ?> trait) {
        return addComponent(getTraits(), trait);
    }

    @Override
    public final boolean removeGene(final AgentTrait<A, ?> gene) {
        return removeComponent(getTraits(), gene);
    }

    @Override
    public final void removeAllGenes() {
        clearComponentList(getTraits());
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
    public final int getId() {
        return getSimulationContext().get().getAgentId();
    }

    @Override
    public final long getTimeOfBirth() {
        return getSimulationContext().get().getActivationStep();
    }

    @Override
    public final long getAge() {
        return getSimulationContext().get().getAge();
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
    public final void logEvent(final Object eventOrigin, final String title, final String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        getSimulationContext().get().logEvent(self(), eventOrigin, title, message);
    }

    @Override
    public final void run() {
        final ActionExecutionStrategy actionExecutionStrategy = getActionExecutionStrategy();
        final boolean executeSuccess = actionExecutionStrategy.execute();
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
     * A hook method which gets called when this agent gets deactivated using {@link org.asoem.greyfish.core.simulation.Simulatable#deactivate()}
     */
    protected void deactivated() {
    }

    @Override
    public final boolean isActive() {
        return getSimulationContext().isPresent();
    }

    @Override
    public final S simulation() {
        checkState(isActive(), "A passive Agent has no associated simulation");
        return getSimulationContext().get().getSimulation();
    }

    @Override
    public final void activate(final SimulationContext<S, A> context) {
        checkNotNull(context);
        setSimulationContext(context);
        logEvent(this, "activated", "");
        activated();
    }

    /**
     * A hook that gets called when this agent was activated with {@link org.asoem.greyfish.core.simulation.Simulatable#activate(SimulationContext)}.
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
        simulation().createAgent(getPopulation(), initializer);
    }
    */

    @Override
    public final Iterable<A> getAllAgents() {
        return simulation().getAgents();
    }

    @Override
    public final Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return simulation().filterAgents(predicate);
    }

    @Override
    public final void sendMessage(final ACLMessage<A> message) {
        simulation().deliverMessage(message);
    }

    @Override
    public final AgentTrait<A, ?> findTrait(final Predicate<? super AgentTrait<A, ?>> traitPredicate) {
        return getTraits().findFirst(traitPredicate).get();
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
     * Get the simulation context.
     * @return the simulation context
     */
    protected abstract Optional<SimulationContext<S, A>> getSimulationContext();

    /**
     * Set the simulation context to {@code simulationContext}.
     * @param simulationContext the new simulation context
     */
    protected abstract void setSimulationContext(@Nullable SimulationContext<S, A> simulationContext);

    /**
     * Get the message box.
     * @return the message box
     */
    protected abstract FunctionalCollection<ACLMessage<A>> getInBox();

    /**
     * Get the action execution strategy.
     * @return the action execution strategy
     */
    protected abstract ActionExecutionStrategy getActionExecutionStrategy();

    @Override
    public final long getSimulationStep() {
        return getSimulationContext().get().getSimulationStep();
    }
}
