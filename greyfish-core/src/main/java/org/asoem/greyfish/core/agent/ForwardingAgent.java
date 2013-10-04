package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.collect.FunctionalList;

import java.awt.*;
import java.util.Set;

public abstract class ForwardingAgent<A extends Agent<A, S>, S extends DiscreteTimeSimulation<A>> extends ForwardingObject implements Agent<A, S> {

    @Override
    protected abstract Agent<A, S> delegate();

    @Override
    public Population getPopulation() {
        return delegate().getPopulation();
    }

    @Override
    public boolean hasPopulation(final Population population) {
        return delegate().hasPopulation(population);
    }

    /**
     * If you wish overwrite {@link Agent#addAction}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
    @Override
    public boolean addAction(final AgentAction<A> action) {
        return delegate().addAction(action);
    }

    @Override
    public boolean removeAction(final AgentAction<A> action) {
        return delegate().removeAction(action);
    }

    @Override
    public void removeAllActions() {
        delegate().removeAllActions();
    }

    @Override
    public FunctionalList<AgentAction<A>> getActions() {
        return delegate().getActions();
    }

    /**
     * If you wish overwrite {@link Agent#addProperty}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
    @Override
    public boolean addProperty(final AgentProperty<A, ?> property) {
        return delegate().addProperty(property);
    }

    @Override
    public boolean removeProperty(final AgentProperty<A, ?> property) {
        return delegate().removeProperty(property);
    }

    @Override
    public void removeAllProperties() {
        delegate().removeAllProperties();
    }

    @Override
    public FunctionalList<AgentProperty<A, ?>> getProperties() {
        return delegate().getProperties();
    }

    @Override
    public AgentProperty<A, ?> getProperty(final String name) {
        return delegate().getProperty(name);
    }

    @Override
    public AgentProperty<A, ?> findProperty(final Predicate<? super AgentProperty<A, ?>> predicate) {
        return delegate().findProperty(predicate);
    }

    @Override
    public void changeActionExecutionOrder(final AgentAction<A> object, final AgentAction<A> object2) {
        delegate().changeActionExecutionOrder(object, object2);
    }

    @Override
    public int getId() {
        return delegate().getId();
    }

    @Override
    public long getTimeOfBirth() {
        return delegate().getTimeOfBirth();
    }

    @Override
    public Color getColor() {
        return delegate().getColor();
    }

    @Override
    public void setColor(final Color color) {
        delegate().setColor(color);
    }

    @Override
    public Iterable<ACLMessage<A>> getMessages(final MessageTemplate template) {
        return delegate().getMessages(template);
    }

    @Override
    public boolean hasMessages(final MessageTemplate template) {
        return delegate().hasMessages(template);
    }

    @Override
    public void logEvent(final Object eventOrigin, final String title, final String message) {
        delegate().logEvent(eventOrigin, title, message);
    }

    @Override
    public void receiveAll(final Iterable<? extends ACLMessage<A>> messages) {
        delegate().receiveAll(messages);
    }

    @Override
    public void receive(final ACLMessage<A> messages) {
        delegate().receive(messages);
    }

    @Override
    public long getAge() {
        return delegate().getAge();
    }

    @Override
    public void run() {
        delegate().run();
    }

    @Override
    public void deactivate(final PassiveSimulationContext<S, A> context) {
        delegate().deactivate(context);
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
    }

    @Override
    public S simulation() {
        return delegate().simulation();
    }

    @Override
    public void activate(final ActiveSimulationContext<S, A> context) {
        delegate().activate(context);
    }

    @Override
    public AgentAction<A> getAction(final String actionName) {
        return delegate().getAction(actionName);
    }

    @Override
    public AgentTrait<A, ?> getTrait(final String geneName) {
        return delegate().getTrait(geneName);
    }

    @Override
    public boolean addTrait(final AgentTrait<A, ?> trait) {
        return delegate().addTrait(trait);
    }

    @Override
    public boolean removeGene(final AgentTrait<A, ?> gene) {
        return delegate().removeGene(gene);
    }

    @Override
    public void removeAllGenes() {
        delegate().removeAllGenes();
    }

    @Override
    public FunctionalList<AgentTrait<A, ?>> getTraits() {
        return delegate().getTraits();
    }

    @Override
    public void initialize() {
        delegate().initialize();
    }

    @Override
    public AgentTrait<A, ?> findTrait(final Predicate<? super AgentTrait<A, ?>> traitPredicate) {
        return delegate().findTrait(traitPredicate);
    }

    @Override
    public Iterable<AgentNode> children() {
        return delegate().children();
    }

    @Override
    public AgentNode parent() {
        return delegate().parent();
    }

    @Override
    public Set<Integer> getParents() {
        return delegate().getParents();
    }

    @Override
    public Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return delegate().filterAgents(predicate);
    }

    @Override
    public Iterable<A> getAllAgents() {
        return delegate().getAllAgents();
    }

    @Override
    public long getSimulationStep() {
        return delegate().getSimulationStep();
    }

    @Override
    public void sendMessage(final ACLMessage<A> message) {
        delegate().sendMessage(message);
    }
}
