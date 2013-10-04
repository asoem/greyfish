package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.impl.agent.BasicAgent;

/**
 * The simulation environment for agents of type {@link org.asoem.greyfish.impl.agent.BasicAgent}.
 */
public interface BasicSimulation extends DiscreteTimeSimulation<BasicAgent> {
    /**
     * Remove agent from this {@code Simulation}.
     * The addition will get effective after the next call to {@link #nextStep()}.
     * @param agent the {@code Agent} to be removed from this {@code Simulation}
     */
    void removeAgent(BasicAgent agent);

    /**
     * Add given {@code agent} to this simulation.
     * The addition will get effective after the next call to {@link #nextStep()}.
     * @param agent the agent to add
     */
    void addAgent(BasicAgent agent);
}
