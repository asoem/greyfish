package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.Coordinates2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.space.TiledSpace;

import java.util.Collection;
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
    public Iterable<MovingObject2D> findObjects(Coordinates2D coordinates, double radius) {
        return delegate().findObjects(coordinates, radius);
    }

    @Override
    public Iterable<Agent> getAgents(Population population) {
        return delegate().getAgents(population);
    }

    @Override
    public Collection<Agent> getAgents() {
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
    public void addSimulationListener(SimulationListener listener) {
        delegate().addSimulationListener(listener);
    }

    @Override
    public void removeSimulationListener(SimulationListener listener) {
        delegate().removeSimulationListener(listener);
    }

    @Override
    public int generateAgentID() {
        return delegate().generateAgentID();
    }

    @Override
    public void createAgent(Population population, Coordinates2D coordinates, Genome genome) {
        delegate().createAgent(population, coordinates, genome);
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
    public Scenario getScenario() {
        return delegate().getScenario();
    }

    @Override
    public void setName(String name) {
        delegate().setName(name);
    }

    @Override
    public void deliverMessage(ACLMessage message) {
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
