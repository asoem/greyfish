package org.asoem.greyfish.core.simulation;

import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.space.WalledTile;
import org.asoem.greyfish.utils.base.Initializer;

import java.util.List;
import java.util.Set;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:51
 */
public abstract class ForwardingSimulation extends ForwardingObject implements Simulation {

    @Override
    protected abstract Simulation delegate();

    @Override
    public int numberOfPopulations() {
        return delegate().numberOfPopulations();
    }

    @Override
    public Iterable<Agent> findNeighbours(Agent agent, double distance) {
        return delegate().findNeighbours(agent, distance);
    }

    @Override
    public Iterable<Agent> getAgents(Population population) {
        return delegate().getAgents(population);
    }

    @Override
    public List<Agent> getAgents() {
        return delegate().getAgents();
    }

    @Override
    public void removeAgent(Agent agent) {
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
    public Set<Agent> getPrototypes() {
        return delegate().getPrototypes();
    }

    @Override
    public TiledSpace<Agent,WalledTile> getSpace() {
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
    public void deliverMessage(ACLMessage<Agent> message) {
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
    public void createAgent(Population population, Initializer<? super Agent> initializer) {
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
}
