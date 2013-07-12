package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.utils.base.HasName;
import org.asoem.greyfish.utils.base.Initializer;

import java.util.Collection;
import java.util.Set;

/**
 * User: christoph
 * Date: 21.11.12
 * Time: 15:32
 */
public interface Simulation<A extends Agent<A, ?>> extends HasName {
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

    void addAgent(A agent);

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
     * Get the number of steps executed so far
     * @return the he number of steps executed so far
     */
    int getSteps();

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

    void logAgentEvent(A agent, Object eventOrigin, String title, String message);

    Iterable<A> filterAgents(Predicate<? super A> predicate);

    boolean hasStepValue(String key);

    void setStepValue(String key, Object value);

    Object getStepValue(String key);
}
