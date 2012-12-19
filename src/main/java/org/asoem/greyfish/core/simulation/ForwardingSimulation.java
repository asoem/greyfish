package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
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
    public Iterable<A> getAgents(Population population) {
        return delegate().getAgents(population);
    }

    @Override
    public Collection<A> getAgents() {
        return delegate().getAgents();
    }

    @Override
    public void removeAgent(A agent) {
        delegate().removeAgent(agent);
    }

    @Override
    public int countAgents() {
        return delegate().countAgents();
    }

    @Override
    public int countAgents(Population population) {
        return delegate().countAgents(population);
    }

    @Override
    public Set<A> getPrototypes() {
        return delegate().getPrototypes();
    }

    @Override
    public int getStep() {
        return delegate().getStep();
    }

    @Override
    public void nextStep() {
        delegate().nextStep();
    }

    @Override
    public void setName(String name) {
        delegate().setName(name);
    }

    @Override
    public void deliverMessage(ACLMessage<A> message) {
        delegate().deliverMessage(message);
    }

    @Override
    public void shutdown() {
        delegate().shutdown();
    }

    @Override
    public Object snapshotValue(String key, Supplier<Object> valueCalculator) {
        return delegate().snapshotValue(key, valueCalculator);
    }

    @Override
    public void createAgent(Population population, Initializer<? super A> initializer) {
        delegate().createAgent(population, initializer);
    }

    @Override
    public void createAgent(Population population) {
        delegate().createAgent(population);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public void logAgentEvent(A agent, Object eventOrigin, String title, String message) {
        delegate().logAgentEvent(agent, eventOrigin, title, message);
    }

    @Override
    public Iterable<A> filterAgents(Predicate<? super A> predicate) {
        return delegate().filterAgents(predicate);
    }
}
