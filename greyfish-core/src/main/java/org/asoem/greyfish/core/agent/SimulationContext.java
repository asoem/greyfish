package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;

/**
 * A SimulationContext is the link between an {@link Agent} and a {@link DiscreteTimeSimulation}.
 * If an agent got activated a newly created context will be set for this agent.
 */
public interface SimulationContext<S extends DiscreteTimeSimulation<A>, A extends Agent<A, S>> {

    /**
     * The step at which this agent was inserted into the simulation.
     * @return the activation step
     */
    long getActivationStep();

    /**
     * Get the id of this agent in this context.
     * @return the id of this agent
     */
    int getAgentId();

    /**
     * Get the simulation for this context.
     * @return the simulation
     */
    S getSimulation();

    /**
     * Get the age of this agent.
     * Same as calling {@code getSimulation().getSteps() - getActivationStep()}
     * @return the difference between the activation step and current step
     */
    long getAge();

    /**
     * Log an event which occurred inside an agent. Should be called from inside an {@link Agent} or {@link
     * org.asoem.greyfish.core.agent.AgentComponent}.
     * Delegates to {@link org.asoem.greyfish.core.simulation.Simulation#logAgentEvent(Agent, Object, String, String)}
     *
     * @param agent       the agent where this event occurred
     * @param eventOrigin the object where this event occurred
     * @param title       the title to log
     * @param message     the message to log
     */
    void logEvent(A agent, Object eventOrigin, String title, String message);

    /**
     * Get the current simulation step.
     * Delegates to {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation#getTime()}
     * @return the number of executed steps in the simulation
     */
    long getSimulationStep();

    /**
     * Check if this context is an active context.
     * @return {@code true} if this context represents an active context, {@code false} if passive
     */
    boolean isActiveContext();
}
