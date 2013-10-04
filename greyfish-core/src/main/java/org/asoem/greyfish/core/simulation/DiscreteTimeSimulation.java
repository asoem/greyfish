package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.utils.DiscreteTime;

/**
 * A Simulation which has discrete time steps.
 */
public interface DiscreteTimeSimulation<A extends Agent<A, ?>> extends Simulation<A>, DiscreteTime {

    /**
     * Get the number of steps executed so far
     * @return the he number of steps executed so far
     */
    @Override
    long getTime();

    /**
     * Proceed on step cycle and execute evaluates agents & commands
     */
    void nextStep();
}
