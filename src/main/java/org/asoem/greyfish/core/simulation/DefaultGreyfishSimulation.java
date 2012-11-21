package org.asoem.greyfish.core.simulation;

import org.apache.commons.pool.KeyedObjectPool;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.utils.space.Point2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:46
 */
public class DefaultGreyfishSimulation extends ParallelizedSimulation<DefaultGreyfishAgent, DefaultGreyfishSimulation, DefaultGreyfishSpace, Point2D> {

    private DefaultGreyfishSimulation(DefaultGreyfishSpace space,
                                      Set<DefaultGreyfishAgent> prototypes,
                                      int parallelizationThreshold, SimulationLogger simulationLogger,
                                      KeyedObjectPool<Population, DefaultGreyfishAgent> agentPool) {
        super(space, prototypes, parallelizationThreshold, simulationLogger, agentPool);
    }

    @Override
    protected DefaultGreyfishSimulation self() {
        return this;
    }
}
