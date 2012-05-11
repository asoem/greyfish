package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 08.03.12
 * Time: 13:07
 */
public interface SimulationContext {
    int getActivationStep();

    @Nullable
    GFAction getLastExecutedAction();

    int getId();

    Simulation getSimulation();

    int getAge();

    void execute(Agent agent);

    void logEvent(Agent agent, Object eventOrigin, String title, String message);

    int getSimulationStep();
}
