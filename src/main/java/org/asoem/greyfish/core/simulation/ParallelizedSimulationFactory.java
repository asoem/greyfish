package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.space.TiledSpace;

/**
 * User: christoph
 * Date: 06.07.12
 * Time: 11:57
 */
public class ParallelizedSimulationFactory implements SimulationFactory<ParallelizedSimulation> {

    private final int parallelizationThreshold;

    public ParallelizedSimulationFactory(int parallelizationThreshold) {
        this.parallelizationThreshold = parallelizationThreshold;
    }

    @Override
    public ParallelizedSimulation createSimulation(TiledSpace<Agent> space) {
        return new ParallelizedSimulation(parallelizationThreshold, space);
    }
}
