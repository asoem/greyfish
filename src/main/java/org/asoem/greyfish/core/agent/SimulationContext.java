package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;

/**
 * User: christoph
 * Date: 08.03.12
 * Time: 13:07
 */
public interface SimulationContext {
    int getActivationStep();

    int getAgentId();

    Simulation getSimulation();

    int getAge();

    void logEvent(Agent agent, Object eventOrigin, String title, String message);

    int getSimulationStep();

    boolean isActiveContext();
}
