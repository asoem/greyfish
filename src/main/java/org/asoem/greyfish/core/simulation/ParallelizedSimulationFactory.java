package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.space.TiledSpace;

/**
 * User: christoph
 * Date: 06.07.12
 * Time: 11:57
 */
public enum ParallelizedSimulationFactory implements SimulationFactory<ParallelizedSimulation> {
    INSTANCE;

    @Override
    public ParallelizedSimulation createSimulation(TiledSpace<Agent> space) {
        return new ParallelizedSimulation(space);
    }
}
