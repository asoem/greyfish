package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.space.Coordinates2D;

import java.util.Collection;
import java.util.Set;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:50
 */
public interface Simulation extends HasName {
    int numberOfPopulations();

    Iterable<Agent> findNeighbours(Agent agent, double radius);

    Iterable<Agent> getAgents(Population population);

    /**
     * @return an unmodifiable view of the list of active individuals
     */
    Collection<Agent> getAgents();

    /**
     * Remove agent from this scenario
     * @param agent
     */
    void removeAgent(Agent agent);

    int countAgents();

    int countAgents(Population population);

    void addSimulationListener(SimulationListener listener);

    void removeSimulationListener(SimulationListener listener);

    int generateAgentID();

    /**
     * Creates a new {@link org.asoem.greyfish.core.individual.Agent} as clone of the prototype registered for given {@code population} with genome set to {@code genome}.
     * The {@link org.asoem.greyfish.core.individual.Agent} will get inserted and executed at the next step at given {@code location}.
     * @param population The {@code Population} of the {@code Prototype} the Agent will be cloned from.
     * @param genome The {@link org.asoem.greyfish.core.genes.ImmutableGenome} for the new {@link org.asoem.greyfish.core.individual.Agent}.
     * @param location
     */
    void createAgent(Population population, Genome genome, Coordinates2D location);

    Set<Agent> getPrototypes();

    TiledSpace getSpace();

    int getSteps();

    /**
     * Proceed on step cycle and execute all agents & commands
     */
    void step();

    Scenario getScenario();

    void setName(String name);

    void deliverMessage(ACLMessage<Agent> message);
}
