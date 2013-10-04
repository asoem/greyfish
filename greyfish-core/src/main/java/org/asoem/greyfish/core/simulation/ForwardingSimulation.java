package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;

import java.util.Collection;

public abstract class ForwardingSimulation<A extends Agent<A, ?>> extends ForwardingObject implements DiscreteTimeSimulation<A> {
    @Override
    protected abstract DiscreteTimeSimulation<A> delegate();

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
    public int countAgents() {
        return delegate().countAgents();
    }

    @Override
    public int countAgents(final Population population) {
        return delegate().countAgents(population);
    }

    @Override
    public long getTime() {
        return delegate().getTime();
    }

    @Override
    public void nextStep() {
        delegate().nextStep();
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

}
