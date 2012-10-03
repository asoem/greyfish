package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.io.ConsoleLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.WalledTileSpace;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 06.07.12
 * Time: 11:57
 */
public class ParallelizedSimulationFactory implements SimulationFactory<ParallelizedSimulation> {

    private final int parallelizationThreshold;
    private final SimulationLogger simulationLogger;

    public ParallelizedSimulationFactory(int parallelizationThreshold) {
        this.parallelizationThreshold = parallelizationThreshold;
        this.simulationLogger = new ConsoleLogger();
    }

    public ParallelizedSimulationFactory(int parallelizationThreshold, SimulationLogger simulationLogger) {
        this.parallelizationThreshold = parallelizationThreshold;
        this.simulationLogger = checkNotNull(simulationLogger);
    }

    @Override
    public ParallelizedSimulation createSimulation(WalledTileSpace<Agent> space, Set<Agent> prototypes) {
        return ParallelizedSimulation.builder(space, prototypes)
                .parallelizationThreshold(parallelizationThreshold)
                .simulationLogger(simulationLogger)
                .build();
    }
}
