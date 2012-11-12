package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CloneFactory;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.io.ConsoleLogger;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.space.Space2D;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 06.07.12
 * Time: 11:57
 */
public class ParallelizedSimulationFactory implements SimulationFactory {

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
    public <A extends Agent, S extends Space2D<A>> ParallelizedSimulation<A,S> createSimulation(S space, Set<? extends A> prototypes, CloneFactory<A> cloneFactory) {
        checkNotNull(space);
        checkNotNull(prototypes);
        checkArgument(!prototypes.contains(null));
        checkNotNull(cloneFactory);

        return ParallelizedSimulation.builder(space, ImmutableSet.copyOf(prototypes))
                .parallelizationThreshold(parallelizationThreshold)
                .simulationLogger(simulationLogger)
                .agentPool(createDefaultAgentPool(prototypes, cloneFactory))
                .build();
    }

    private <A extends Agent> KeyedObjectPool<Population, A> createDefaultAgentPool(final Set<? extends A> prototypes, final CloneFactory<A> cloneFactory) {
        return new StackKeyedObjectPool<Population, A>(
                new BaseKeyedPoolableObjectFactory<Population, A>() {

                    final Map<Population, ? extends A> populationPrototypeMap =
                            Maps.uniqueIndex(prototypes, new Function<Agent, Population>() {
                                @Override
                                public Population apply(Agent input) {
                                    return input.getPopulation();
                                }
                            });

                    @Override
                    public A makeObject(Population key) throws Exception {
                        assert key != null;


                        final A prototype = populationPrototypeMap.get(key);
                        assert prototype != null : "Found no Prototype for " + key;

                        return cloneFactory.cloneAgent(prototype);
                    }
                },
                10000, 100);
    }
}
