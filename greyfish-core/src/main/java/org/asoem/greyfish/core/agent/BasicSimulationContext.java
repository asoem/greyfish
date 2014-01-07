package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * A SimulationContext is the link between an {@link Agent} and a {@link DiscreteTimeSimulation}. If an agent got
 * activated a newly created context will be set for this agent.
 */
public interface BasicSimulationContext<S extends DiscreteTimeSimulation<?>, A extends Agent<?>> extends SimulationContext<S, A> {

    /**
     * The step at which this agent was inserted into the getSimulation.
     *
     * @return the activation step
     */
    long getActivationStep();

    /**
     * Get the age of this agent. Same as calling {@code getSimulation().getSteps() - getActivationStep()}
     *
     * @return the difference between the activation step and current step
     */
    long getAge();

    /**
     * Get the current getSimulation step. Delegates to {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation#getTime()}
     *
     * @return the number of executed steps in the getSimulation
     */
    long getSimulationStep();

    /**
     * Get the current simulation time. <p>Same as calling {@code getSimulation().getTime()}</p>
     *
     * @return the current time of the simulation
     */
    long getTime();

    String simulationName();
}
