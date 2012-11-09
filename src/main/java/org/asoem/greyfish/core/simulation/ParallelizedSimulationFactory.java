package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.io.ConsoleLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.Space2D;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 06.07.12
 * Time: 11:57
 */
public class ParallelizedSimulationFactory<A extends Agent, S extends Space2D<A>> implements SimulationFactory<ParallelizedSimulation,A,S> {

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
    public ParallelizedSimulation createSimulation(S space, Set<A> prototypes) {
        return ParallelizedSimulation.builder(space, prototypes)
                .parallelizationThreshold(parallelizationThreshold)
                .simulationLogger(simulationLogger)
                .build();
    }
}
