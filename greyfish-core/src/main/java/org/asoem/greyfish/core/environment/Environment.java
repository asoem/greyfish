package org.asoem.greyfish.core.environment;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.PrototypeGroup;

/**
 * A {@code Simulation} is an environment for interacting agents.
 */
public interface Environment<A extends Agent<?>> {
    /**
     * Get all active {@code Agent}s which are part of the given {@code prototypeGroup}.
     *
     * @param prototypeGroup The common prototypeGroup
     * @return An iterable of all active {@code Agents} with prototypeGroup equal to {@code prototypeGroup}
     */
    Iterable<A> getAgents(PrototypeGroup prototypeGroup);

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
    Iterable<A> getActiveAgents();

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
     * Count all active agents with prototypeGroup equal to {@code prototypeGroup}.
     *
     * @param prototypeGroup the {@code PrototypeGroup} of the agents to count
     * @return the number of all active agents with prototypeGroup equal to {@code prototypeGroup}
     */
    int countAgents(PrototypeGroup prototypeGroup);

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
     * Get a status info for this simulation. The content is implementation specific.
     *
     * @return a message providing information about this simulation
     */
    String getStatusInfo();
}