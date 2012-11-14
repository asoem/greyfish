package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 08.03.12
 * Time: 13:07
 */
public interface SimulationContext<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> {
    int getActivationStep();

    int getAgentId();

    S getSimulation();

    int getAge();

    void logEvent(A agent, Object eventOrigin, String title, String message);

    int getSimulationStep();

    boolean isActiveContext();
}
