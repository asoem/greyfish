package org.asoem.greyfish.core.agent;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageBox;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

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
    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(AbstractAgent.class);

    private static <E extends HasName> E findByName(FunctionalList<E> functionalList, final String name) {
        return functionalList.find(new Predicate<HasName>() {
            @Override
            public boolean apply(HasName agentAction) {
                return agentAction.getName().equals(name);
            }
        });
    }

    protected abstract A self();

    @Override
    public boolean hasPopulation(@Nullable Population population) {
        return Objects.equal(getPopulation(), population);
    }

    private <E extends AgentComponent<A>> boolean addComponent(FunctionalList<E> list, E element) {
        if (list.add(element)) {
            element.setAgent(self());
            return true;
        }
        return false;
    }

    private <E extends AgentComponent<A>> boolean removeComponent(FunctionalList<? extends E> list, E element) {
        if (list.remove(element)) {
            element.setAgent(self());
            return true;
        }
        return false;
    }

    private void clearComponentList(FunctionalList<? extends AgentComponent<A>> list) {
        List<AgentComponent<A>> temp = ImmutableList.copyOf(list);
        list.clear();
        for (AgentComponent<A> component : temp)
            component.setAgent(null);
    }

    @Override
    public boolean addAction(AgentAction<A> action) {
        return addComponent(getActions(), action);
    }

    @Override
    public boolean removeAction(AgentAction<A> action) {
        return removeComponent(getActions(), action);
    }

    @Override
    public void removeAllActions() {
        clearComponentList(getActions());
    }

    @Override
    public AgentAction<A> getAction(String name) {
        return findByName(getActions(), name);
    }

    @Override
    public boolean addProperty(AgentProperty<A, ?> property) {
        return addComponent(getProperties(), property);
    }

    @Override
    public boolean removeProperty(AgentProperty<A, ?> property) {
        return removeComponent(getProperties(), property);
    }

    @Override
    public void removeAllProperties() {
        clearComponentList(getProperties());
    }

    @Override
    public AgentProperty<A, ?> getProperty(String name) {
        return findByName(getProperties(), name);
    }

    @Override
    public AgentProperty<A, ?> findProperty(Predicate<? super AgentProperty<A, ?>> predicate) {
        return getProperties().find(predicate);
    }

    @Override
    public boolean addTrait(AgentTrait<A, ?> gene) {
        return addComponent(getTraits(), gene);
    }

    @Override
    public boolean removeGene(AgentTrait<A, ?> gene) {
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
    public AgentTrait<A, ?> getTrait(String name) {
        return findByName(getTraits(), name);
    }

    @Override
    public void changeActionExecutionOrder(AgentAction<A> object, AgentAction<A> object2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void receive(AgentMessage<A> message) {
        LOGGER.debug("{} received a message: {}", this, message);
        getInBox().add(message);
    }

    @Override
    public void receiveAll(Iterable<? extends AgentMessage<A>> messages) {
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
    public void setColor(Color color) {
    }

    @Override
    public Iterable<AgentMessage<A>> getMessages(MessageTemplate template) {
        return getInBox().extract(template);
    }

    @Override
    public boolean hasMessages(MessageTemplate template) {
        return Iterables.any(getInBox(), template);
    }

    @Override
    public void logEvent(Object eventOrigin, String title, String message) {
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        getSimulationContext().logEvent(self(), eventOrigin, title, message);
    }

    @Override
    public void execute() {
        getActionExecutionStrategy().execute();
        LOGGER.info("{} executed {}", this, getActionExecutionStrategy().lastExecutedAction());
    }

    @Override
    public void deactivate(SimulationContext<S, A> context) {
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
    public void activate(SimulationContext<S, A> context) {
        checkNotNull(context);
        setSimulationContext(context);
        getActionExecutionStrategy().reset();
        logEvent(this, "activated", "");
    }

    @Override
    public void initialize() {
        for (AgentNode node : children()) {
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
    public Iterable<A> filterAgents(Predicate<? super A> predicate) {
        return simulation().filterAgents(predicate);
    }

    @Override
    public void die() {
        simulation().removeAgent(self());
    }

    @Override
    public void sendMessage(ACLMessage<A> message) {
        simulation().deliverMessage(message);
    }

    @Override
    public AgentTrait<A, ?> findTrait(Predicate<? super AgentTrait<A, ?>> traitPredicate) {
        return getTraits().find(traitPredicate);
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
