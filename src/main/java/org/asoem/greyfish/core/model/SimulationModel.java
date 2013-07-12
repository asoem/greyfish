package org.asoem.greyfish.core.model;

import org.asoem.greyfish.core.simulation.Simulation;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:56
 */
public interface SimulationModel<S extends Simulation<?>> {
    S createSimulation();
}
