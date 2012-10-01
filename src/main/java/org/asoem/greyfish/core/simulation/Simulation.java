package org.asoem.greyfish.core.simulation;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.space.WalledTile;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.base.Initializer;

import java.util.List;
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
    List<Agent> getAgents();

    /**
     * Remove agent from this {@code Simulation}
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void removeAgent(Agent agent);

    int countAgents();

    int countAgents(Population population);

    int generateAgentID();

    Set<Agent> getPrototypes();

    TiledSpace<Agent,WalledTile> getSpace();

    int getStep();

    /**
     * Proceed on step cycle and execute evaluates agents & commands
     */
    void nextStep();

    void setName(String name);

    void deliverMessage(ACLMessage<Agent> message);

    void shutdown();

    UUID getUUID();

    void setSimulationLogger(SimulationLogger simulationLogger);

    void logEvent(int agentId, String populationName, double[] coordinates, Object eventOrigin, String title, String message);

    /**
     * With this method you can store values which hold for all agents during a single simulation step.
     * Using these snapshot values, you can prevent duplicate computations.
     * @param key The name of the value
     * @param valueCalculator The algorithm to compute the value
     * @return The stored value for {@code key}
     */
    Object snapshotValue(String key, Supplier<Object> valueCalculator);

    void createAgent(Population population, Initializer<Agent> initializer);
}
