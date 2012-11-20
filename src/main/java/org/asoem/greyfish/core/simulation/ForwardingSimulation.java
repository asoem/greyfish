package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collection;
import java.util.Set;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:51
 */
public abstract class ForwardingSimulation<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, P>, Z extends Space2D<A, P>, P extends Object2D> extends ForwardingObject implements Simulation<S,A,Z,P> {

    @Override
    protected abstract Simulation<S,A,Z,P> delegate();

    @Override
    public int numberOfPopulations() {
        return delegate().numberOfPopulations();
    }

    @Override
    public Iterable<A> findNeighbours(A agent, double distance) {
        return delegate().findNeighbours(agent, distance);
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
    public Z getSpace() {
        return delegate().getSpace();
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
    public SimulationLogger getSimulationLogger() {
        return delegate().getSimulationLogger();
    }

    @Override
    public void logAgentEvent(int agentId, String populationName, double[] coordinates, Object eventOrigin, String title, String message) {
        delegate().logAgentEvent(agentId, populationName, coordinates, eventOrigin, title, message);
    }

    @Override
    public Iterable<A> filterAgents(Predicate<? super A> predicate) {
        return delegate().filterAgents(predicate);
    }
}
