package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.space.TiledSpace;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 15:55
 */
public interface SimulationFactory<T extends Simulation> {
    T createSimulation(TiledSpace<Agent> space);
}
