package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;

import java.util.Collection;

/**
 * A {@code Simulation} is an environment for interacting agents.
 */
public interface Simulation<A extends Agent<A, ?>> {
    /**
     * Get all active {@code Agent}s which are part of the given {@code population}.
     *
     * @param population The common population
     * @return An iterable of all active {@code Agents} with population equal to {@code population}
     */
    Iterable<A> getAgents(Population population);

    /**
     * Get all agents in this simulation which satisfy the given {@code predicate}.
     *
     * @param predicate the predicate to check each agent against
     * @return all agents which satisfy the given {@code predicate}
     */
    Iterable<A> filterAgents(Predicate<? super A> predicate);

    /**
     * Get all agents in this simulation.
     *
     * @return an unmodifiable view of all active {@code Agent}s
     */
    Collection<A> getAgents();

    /**
     * The number of registered populations which is equal to the number of register prototypes.
     *
     * @return The number of populations registered for this simulation
     */
    int numberOfPopulations();

    /**
     * Count all active Agents.
     *
     * @return The number of active Agents
     */
    int countAgents();

    /**
     * Count all active agents with population equal to {@code population}.
     *
     * @param population the {@code Population} of the agents to count
     * @return the number of all active agents with population equal to {@code population}
     */
    int countAgents(Population population);

    /**
     * @return the name of this simulation
     */
    String getName();

    /**
     * Deliver given {@code message} to its destination.
     *
     * @param message the message to deliver
     */
    void deliverMessage(ACLMessage<A> message);

    /**
     * Shutdown this simulation and clean up resources.
     */
    void shutdown();

    /**
     * Log an event which occurred inside an agent. Should be called from inside an {@link Agent} or {@link
     * org.asoem.greyfish.core.agent.AgentComponent}.
     *
     * @param agent       the agent where this event occurred
     * @param eventOrigin the object where this event occurred
     * @param title       the title to log
     * @param message     the message to log
     */
    void logAgentEvent(A agent, Object eventOrigin, String title, String message);
}
