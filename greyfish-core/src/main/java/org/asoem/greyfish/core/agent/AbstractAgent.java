package org.asoem.greyfish.core.agent;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageBox;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 20.11.12
 * Time: 17:18
 */
public abstract class AbstractAgent<A extends Agent<A, S>, S extends Simulation<A>> implements Agent<A, S> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgent.class);

    private static <E extends AgentComponent> E findByName(final FunctionalList<E> functionalList, final String name) {
        return functionalList.findFirst(new Predicate<AgentComponent>() {
            @Override
            public boolean apply(final AgentComponent agentAction) {
                return agentAction.getName().equals(name);
            }
        }).get();
    }

    protected abstract A self();

    @Override
    public boolean hasPopulation(@Nullable final Population population) {
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
    public boolean addAction(final AgentAction<A> action) {
        return addComponent(getActions(), action);
    }

    @Override
    public boolean removeAction(final AgentAction<A> action) {
        return removeComponent(getActions(), action);
    }

    @Override
    public void removeAllActions() {
        clearComponentList(getActions());
    }

    @Override
    public AgentAction<A> getAction(final String name) {
        return findByName(getActions(), name);
    }

    @Override
    public boolean addProperty(final AgentProperty<A, ?> property) {
        return addComponent(getProperties(), property);
    }

    @Override
    public boolean removeProperty(final AgentProperty<A, ?> property) {
        return removeComponent(getProperties(), property);
    }

    @Override
    public void removeAllProperties() {
        clearComponentList(getProperties());
    }

    @Override
    public AgentProperty<A, ?> getProperty(final String name) {
        return findByName(getProperties(), name);
    }

    @Override
    public final AgentProperty<A, ?> findProperty(final Predicate<? super AgentProperty<A, ?>> predicate) {
        return getProperties().findFirst(predicate).get();
    }

    @Override
    public boolean addTrait(final AgentTrait<A, ?> trait) {
        return addComponent(getTraits(), trait);
    }

    @Override
    public boolean removeGene(final AgentTrait<A, ?> gene) {
        return removeComponent(getTraits(), gene);
    }

    @Override
    public void removeAllGenes() {
        clearComponentList(getTraits());
    }

    @Override
    public abstract FunctionalList<AgentTrait<A, ?>> getTraits();

    @Override
    @Nullable
    public AgentTrait<A, ?> getTrait(final String name) {
        return findByName(getTraits(), name);
    }

    @Override
    public void changeActionExecutionOrder(final AgentAction<A> object, final AgentAction<A> object2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void receive(final AgentMessage<A> message) {
        LOGGER.debug("{} received a message: {}", this, message);
        getInBox().add(message);
    }

    @Override
    public void receiveAll(final Iterable<? extends AgentMessage<A>> messages) {
        LOGGER.debug("{} received {} messages: {}", this, Iterables.size(messages), messages);
        Iterables.addAll(getInBox(), messages);
    }

    @Override
    public int getId() {
        return getSimulationContext().getAgentId();
    }

    @Override
    public int getTimeOfBirth() {
        return getSimulationContext().getActivationStep();
    }

    @Override
    public int getAge() {
        return getSimulationContext().getAge();
    }

    @Override
    @Nullable
    public Color getColor() {
        final Population population = getPopulation();
        return (population != null) ? population.getColor() : null;
    }

    @Override
    public void setColor(final Color color) {
    }

    @Override
    public Iterable<AgentMessage<A>> getMessages(final MessageTemplate template) {
        return getInBox().extract(template);
    }

    @Override
    public boolean hasMessages(final MessageTemplate template) {
        return Iterables.any(getInBox(), template);
    }

    @Override
    public void logEvent(final Object eventOrigin, final String title, final String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        getSimulationContext().logEvent(self(), eventOrigin, title, message);
    }

    @Override
    public void execute() {
        final ActionExecutionStrategy actionExecutionStrategy = getActionExecutionStrategy();
        final boolean executeSuccess = actionExecutionStrategy.execute();
        if (executeSuccess) {
            LOGGER.debug("{} executed {}", this, actionExecutionStrategy);
        }
    }

    @Override
    public void deactivate(final SimulationContext<S, A> context) {
        checkNotNull(context);
        setSimulationContext(context);
        getInBox().clear();
    }

    @Override
    public boolean isActive() {
        return getSimulationContext().isActiveContext();
    }

    @Override
    public S simulation() {
        checkState(isActive(), "A passive Agent has no associated simulation");
        return getSimulationContext().getSimulation();
    }

    @Override
    public void freeze() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void activate(final SimulationContext<S, A> context) {
        checkNotNull(context);
        setSimulationContext(context);
        getActionExecutionStrategy().reset();
        logEvent(this, "activated", "");
    }

    @Override
    public void initialize() {
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
    public Iterable<A> getAllAgents() {
        return simulation().getAgents();
    }

    @Override
    public Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return simulation().filterAgents(predicate);
    }

    @Override
    public void die() {
        simulation().removeAgent(self());
    }

    @Override
    public void sendMessage(final ACLMessage<A> message) {
        simulation().deliverMessage(message);
    }

    @Override
    public AgentTrait<A, ?> findTrait(final Predicate<? super AgentTrait<A, ?>> traitPredicate) {
        return getTraits().findFirst(traitPredicate).get();
    }

    @Override
    public Iterable<AgentNode> children() {
        return Iterables.<AgentNode>concat(
                getProperties(),
                getActions(),
                getTraits()
        );
    }

    @Override
    public AgentNode parent() {
        return null;
    }

    protected abstract SimulationContext<S, A> getSimulationContext();

    protected abstract MessageBox<AgentMessage<A>> getInBox();

    protected abstract void setSimulationContext(SimulationContext<S, A> simulationContext);

    protected abstract ActionExecutionStrategy getActionExecutionStrategy();

    @Override
    public int getSimulationStep() {
        return getSimulationContext().getSimulationStep();
    }
}
