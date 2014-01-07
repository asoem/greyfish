package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * A getSimulation which executes it's agents in discrete steps.
 * Before each step all agents are synchronized, to ensure that they all share the same knowledge.
 */
public interface SynchronizedAgentsSimulation<A extends Agent<?>> extends DiscreteTimeSimulation<A> {
    /**
     * Proceed one step cycle and execute all agents
     */
    void nextStep();
}
