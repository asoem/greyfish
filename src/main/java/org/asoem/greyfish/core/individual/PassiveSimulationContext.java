package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.simpleframework.xml.Root;

/**
 * User: christoph
 * Date: 08.03.12
 * Time: 13:08
 */
@Root
public enum PassiveSimulationContext implements SimulationContext {

    INSTANCE;

    @Override
    public int getActivationStep() {
        return -1;
    }

    @Override
    public GFAction getLastExecutedAction() {
        return null;
    }

    @Override
    public int getId() {
        return -1;
    }

    @Override
    public Simulation getSimulation() {
        return null;
    }

    @Override
    public int getAge() {
        return -1;
    }

    @Override
    public void execute(Agent agent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logEvent(Agent agent, Object eventOrigin, String title, String message) {
        throw new UnsupportedOperationException();
    }

    public static PassiveSimulationContext instance() {
        return INSTANCE;
    }
}
