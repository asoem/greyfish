package org.asoem.greyfish.core.simulation;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Collection;
import java.util.Set;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 10:50
 */
public interface Simulation<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> extends HasName {

    /**
     * Get all active {@code Agent}s which are part of the given {@code population}
     * @param population The common population
     * @return An iterable of all active {@code Agents} with population equal to {@code population}
     */
    Iterable<A> getAgents(Population population);

    /**
     * @return an unmodifiable view of all active {@code Agent}s
     */
    Collection<A> getAgents();

    /**
     * Create a new {@code Agent} as a clone of the prototype registered for given {@code population}
     * and initialized with the given {@code initializer}.
     * The clone will be active the at the next call to {@link #nextStep()}.
     * @param population the population of the prototype to use for cloning
     * @param initializer the initializer which will be applied on constructed {@code Agent}
     */
    void createAgent(Population population, Initializer<? super A> initializer);

    void createAgent(Population population);

    /**
     * Remove agent from this {@code Simulation}
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void removeAgent(A agent);

    /**
     * The number of registered populations which is equal to the number of register prototypes.
     * @return The number of populations registered for this simulation
     */
    int numberOfPopulations();

    /**
     * Find all neighbours of {@code agent} within the given {@code distance}
     *
     * @param agent The focal {@code agent}
     * @param distance The maximum allowed distance of an agent to count as a neighbour
     * @return all neighbours of {@code agent} within the given distance
     */
    Iterable<A> findNeighbours(A agent, double distance);

    /**
     * Count all active Agents
     * @return The number of active Agents
     */
    int countAgents();

    /**
     * Count all active agents with population equal to {@code population}
     * @param population the {@code Population} of the agents to count
     * @return the number of all active agents with population equal to {@code population}
     */
    int countAgents(Population population);

    /**
     * Get all registered prototypes
     * @return all registered {@code Agent}s which are used as prototypes
     */
    Set<A> getPrototypes();

    /**
     * Get the space used in this simulation
     * @return the space used in this simulation
     */
    Z getSpace();

    /**
     * Get the current step
     * @return the current step
     */
    int getStep();

    /**
     * Proceed on step cycle and execute evaluates agents & commands
     */
    void nextStep();

    /**
     * Set the name of this simulation
     * @param name the new name for this simulation
     */
    void setName(String name);

    /**
     * Deliver given {@code message} to its destination
     * @param message the message to deliver
     */
    void deliverMessage(ACLMessage<A> message);

    /**
     * Shutdown this simulation and clean up resources
     */
    void shutdown();

    /**
     * Get the {@code SimulationLogger} used in this simulation
     * @return the {@code SimulationLogger} used in this simulation
     */
    SimulationLogger getSimulationLogger();

    void logAgentEvent(int agentId, String populationName, double[] coordinates, Object eventOrigin, String title, String message);

    /**
     * With this method you can store values which hold for all agents during a single simulation step.
     * Using these snapshot values, you can prevent duplicate computations.
     * @param key The name of the value
     * @param valueCalculator The algorithm to compute the value
     * @return The stored value for {@code key}
     */
    Object snapshotValue(String key, Supplier<Object> valueCalculator);
}
