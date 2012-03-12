package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.io.AgentEvent;
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
    public int getFirstStep() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GFAction getLastExecutedAction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Simulation getSimulation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAge() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Agent agent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logEvent(AgentEvent event) {
        throw new UnsupportedOperationException();
    }

    public static PassiveSimulationContext instance() {
        return INSTANCE;
    }
}
