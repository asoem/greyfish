package org.asoem.greyfish.core.simulation;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.space.WalledTile;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.space.Object2D;

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

    /**
     * Create an {@code Agent} of type {@code population}, that is, a clone of the prototype registered for {@code population}.
     * This method does not activate the agent, so you have to call {@link #activateAgent(org.asoem.greyfish.core.individual.Agent, org.asoem.greyfish.utils.space.Object2D)} afterwards.
     * @param population the type of the created {@code Agent}
     * @return an {@code Agent} of type {@code population}
     */
    Agent createAgent(final Population population);

    /**
     * Activate the given {@code agent} in the next step represented in space by the given {@code projection}.
     * @param agent the agent to activate
     * @param projection the agent's representation in {@link org.asoem.greyfish.core.space.Space2D}
     */
    void activateAgent(Agent agent, Object2D projection);

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

    void createEvent(int agentId, String populationName, double[] coordinates, Object eventOrigin, String title, String message);

    /**
     * With this method you can store values which hold for all agents during a single simulation step.
     * Using these snapshot values, you can prevent duplicate computations.
     * @param key The name of the value
     * @param valueCalculator The algorithm to compute the value
     * @return The stored value for {@code key}
     */
    Object snapshotValue(String key, Supplier<Object> valueCalculator);
}
