package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.space.TiledSpace;

import java.util.Set;
import java.util.UUID;

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
    public Iterable<Agent> findNeighbours(Agent agent, double radius) {
        return delegate().findNeighbours(agent, radius);
    }

    @Override
    public Iterable<Agent> getAgents(Population population) {
        return delegate().getAgents(population);
    }

    @Override
    public Iterable<Agent> getAgents() {
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
    public int countAgents(String populationName) {
        return delegate().countAgents(populationName);
    }

    @Override
    public int generateAgentID() {
        return delegate().generateAgentID();
    }

    @Override
    public Set<Agent> getPrototypes() {
        return delegate().getPrototypes();
    }

    @Override
    public TiledSpace<Agent> getSpace() {
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
    public UUID getUUID() {
        return delegate().getUUID();
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

}
