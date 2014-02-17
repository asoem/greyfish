package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.impl.agent.BasicAgent;

import java.util.concurrent.Executor;

/**
 * The getSimulation environment for agents of type {@link org.asoem.greyfish.impl.agent.BasicAgent}.
 */
public interface BasicSimulation extends SynchronizedAgentsSimulation<BasicAgent> {
    /**
     * Add the removal of given {@code agent} to this simulations modification queue.
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void enqueueRemoval(BasicAgent agent);

    /**
     * Add the removal of given {@code agent} to this simulations modification queue.
     *
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void enqueueRemoval(BasicAgent agent, Runnable listener, Executor executor);

    /**
     * Add the addition of given {@code agent} to this simulations modification queue.
     *
     * @param agent the agent to add
     */
    void enqueueAddition(BasicAgent agent);
}
