package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.space.WalledTileSpace;

import java.util.Set;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 15:55
 */
public interface SimulationFactory<T extends Simulation> {
    T createSimulation(WalledTileSpace<Agent> space, Set<Agent> prototypes);
}
