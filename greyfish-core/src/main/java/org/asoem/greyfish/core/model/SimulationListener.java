package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.Simulation;

public interface SimulationListener {
    void started(Simulation<?> simulation);
    void done(Simulation<?> simulation);
}
