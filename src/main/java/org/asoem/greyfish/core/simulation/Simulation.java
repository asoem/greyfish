package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.space.Location2D;

import java.util.Set;
import java.util.UUID;

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
     * @return an unmodifiable view of evaluates active {@code Agent}s
     */
    Iterable<Agent> getAgents();

    /**
     * Remove agent from this {@code Simulation}
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void removeAgent(Agent agent);

    int countAgents();

    int countAgents(Population population);

    int generateAgentID();

    /**
     * Creates a new {@link org.asoem.greyfish.core.individual.Agent} as clone of the prototype registered for given {@code population} with chromosome set to {@code chromosome}.
     * The {@link org.asoem.greyfish.core.individual.Agent} will get inserted before and executed on the next step at given {@code location}.
     * @param population The {@code Population} of the {@code AgentModel} the Agent will be cloned from.
     * @param chromosome The chromosome which will be injected into the inserted Agent ({@link Agent#injectGamete(org.asoem.greyfish.core.genes.Chromosome)}).
     * @param location the location where the {@code Agent} will enter the {@code Simulation}
     */
    void createAgent(Population population, Chromosome<? extends Gene<?>> chromosome, Location2D location);

    Set<Agent> getPrototypes();

    TiledSpace<Agent> getSpace();

    int getSteps();

    /**
     * Proceed on step cycle and execute evaluates agents & commands
     */
    void step();

    void setName(String name);

    void deliverMessage(ACLMessage<Agent> message);

    void shutdown();

    UUID getUUID();

    void createEvent(int agentId, String populationName, double[] coordinates, Object eventOrigin, String title, String message);
}
