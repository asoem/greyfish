package org.asoem.greyfish.core.environment;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.Agent;

/**
 * A {@code Simulation} is an environment for interacting agents.
 */
public interface Enironment<A extends Agent<?>> {

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
     * Count all active Agents.
     *
     * @return The number of active Agents
     */
    int countAgents();

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
