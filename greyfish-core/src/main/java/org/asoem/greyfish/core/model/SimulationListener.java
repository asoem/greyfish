package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.Environment;

public interface SimulationListener {
    void started(Environment<?> environment);

    void done(Environment<?> environment);
}
