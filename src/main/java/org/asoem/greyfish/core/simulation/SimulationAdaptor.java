package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.Coordinates2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:51
 */
public abstract class SimulationAdaptor implements Simulation {
    
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
    public int generateAgentID() {
        return delegate().generateAgentID();
    }

    @Override
    public void createAgent(Population population, Genome<? extends Gene<?>> genome, Coordinates2D location) {
        delegate().createAgent(population, genome, location);
    }

    @Override
    public Set<Agent> getPrototypes() {
        return delegate().getPrototypes();
    }

    @Override
    public TiledSpace getSpace() {
        return delegate().getSpace();
    }

    @Override
    public int getSteps() {
        return delegate().getSteps();
    }

    @Override
    public void step() {
        delegate().step();
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
    public String getName() {
        return delegate().getName();
    }

    @Override
    public boolean hasName(String s) {
        return delegate().hasName(s);
    }
}
