package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.utils.base.Initializer;

import java.util.Collection;
import java.util.Set;

/**
 * User: christoph
 * Date: 21.11.12
 * Time: 15:39
 */
public abstract class ForwardingSimulation<A extends Agent<A, ?>> extends ForwardingObject implements Simulation<A> {
    @Override
    protected abstract Simulation<A> delegate();

    @Override
    public int numberOfPopulations() {
        return delegate().numberOfPopulations();
    }

    @Override
    public Iterable<A> getAgents(final Population population) {
        return delegate().getAgents(population);
    }

    @Override
    public Collection<A> getAgents() {
        return delegate().getAgents();
    }

    @Override
    public void removeAgent(final A agent) {
        delegate().removeAgent(agent);
    }

    @Override
    public int countAgents() {
        return delegate().countAgents();
    }

    @Override
    public int countAgents(final Population population) {
        return delegate().countAgents(population);
    }

    @Override
    public Set<A> getPrototypes() {
        return delegate().getPrototypes();
    }

    @Override
    public int getSteps() {
        return delegate().getSteps();
    }

    @Override
    public void nextStep() {
        delegate().nextStep();
    }

    @Override
    public void setName(final String name) {
        delegate().setName(name);
    }

    @Override
    public void deliverMessage(final ACLMessage<A> message) {
        delegate().deliverMessage(message);
    }

    @Override
    public void shutdown() {
        delegate().shutdown();
    }

    @Override
    public void createAgent(final Population population, final Initializer<? super A> initializer) {
        delegate().createAgent(population, initializer);
    }

    @Override
    public void createAgent(final Population population) {
        delegate().createAgent(population);
    }

    @Override
    public void addAgent(final A agent) {
        delegate().addAgent(agent);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public void logAgentEvent(final A agent, final Object eventOrigin, final String title, final String message) {
        delegate().logAgentEvent(agent, eventOrigin, title, message);
    }

    @Override
    public Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return delegate().filterAgents(predicate);
    }

    @Override
    public boolean hasStepValue(final String key) {
        return delegate().hasStepValue(key);
    }

    @Override
    public void setStepValue(final String key, final Object value) {
        delegate().setStepValue(key, value);
    }

    @Override
    public Object getStepValue(final String key) {
        return delegate().getStepValue(key);
    }
}
