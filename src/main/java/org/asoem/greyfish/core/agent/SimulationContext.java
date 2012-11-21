package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.SpatialSimulation;

/**
 * User: christoph
 * Date: 08.03.12
 * Time: 13:07
 */
public interface SimulationContext<S extends SpatialSimulation<A, ?>, A extends Agent<A, S, ?>> {
    int getActivationStep();

    int getAgentId();

    S getSimulation();

    int getAge();

    void logEvent(A agent, Object eventOrigin, String title, String message);

    int getSimulationStep();

    boolean isActiveContext();
}
