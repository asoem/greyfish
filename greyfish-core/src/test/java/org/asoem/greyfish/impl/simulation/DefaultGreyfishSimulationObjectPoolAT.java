package org.asoem.greyfish.impl.simulation;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.actions.GenericAction;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.DefaultBasic2DAgent;
import org.asoem.greyfish.impl.space.DefaultGreyfishTiled2DSpace;
import org.asoem.greyfish.impl.space.DefaultGreyfishTiled2DSpaceImpl;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class DefaultGreyfishSimulationObjectPoolAT {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGreyfishSimulationImplTest.class);

    @Test
    public void acceptObjectPoolPerformance() throws Exception {
        // given
        final int populationSize = 400;
        final int steps = 1000;
        final int runs = 100;
        final SummaryStatistics statisticsWithoutObjectPool = new SummaryStatistics();
        final SummaryStatistics statisticsWithObjectPool = new SummaryStatistics();

        // when
        for (int i = 0; i < runs; i++) {
            // randomize execution order
            if (RandomGenerators.rng().nextBoolean()) {
                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(createSimulationWithoutObjectPool(populationSize), steps));

                statisticsWithObjectPool.addValue(
                        measureExecutionTime(createSimulationWithObjectPool(populationSize), steps));
            } else {
                statisticsWithObjectPool.addValue(
                        measureExecutionTime(createSimulationWithObjectPool(populationSize), steps));

                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(createSimulationWithoutObjectPool(populationSize), steps));
            }
        }

        // then
        LOGGER.info("Simulation with object pool vs. without object pool: {}, {}",
                statisticsWithObjectPool, statisticsWithoutObjectPool);

        assertThat("The mean elapsed time of the simulation with an object pool " +
                "is not less than the mean elapsed time of the version with an object pool",
                statisticsWithObjectPool.getMean(), is(lessThan(statisticsWithoutObjectPool.getMean())));

        final double p = new TTest().tTest(statisticsWithObjectPool, statisticsWithoutObjectPool);
        LOGGER.info("t-test: p={}", p);
        assertThat("The measured difference is not significant", p, is(lessThan(0.05)));
    }

    private long measureExecutionTime(final Basic2DSimulation simulationWithoutObjectPool, final int steps) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < steps; j++) {
            simulationWithoutObjectPool.nextStep();
        }
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private Basic2DSimulation createSimulationWithObjectPool(final int populationSize) {

        final Map<Population, Basic2DAgent> agentMap = Maps.newHashMap();

        final KeyedObjectPool<Population, Basic2DAgent> objectPool = new StackKeyedObjectPool<Population, Basic2DAgent>(new BaseKeyedPoolableObjectFactory<Population, Basic2DAgent>() {
            @Override
            public Basic2DAgent makeObject(final Population population) throws Exception {
                return CycleCloner.clone(agentMap.get(population));
            }
        }, populationSize / 2);

        final DefaultGreyfishTiled2DSpace space = DefaultGreyfishTiled2DSpaceImpl.ofSize(1, 1);
        final Population population = Population.named("test");
        final DefaultBasic2DAgent prototype = DefaultBasic2DAgent.builder(population)
                .addActions(
                        GenericAction.<Basic2DAgent>builder()
                                .name("reproduce")
                                .executedIf(GenericCondition.<Basic2DAgent>evaluate(Callbacks.random()))
                                .executes(new Callback<GenericAction<Basic2DAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<Basic2DAgent> caller, final Map<String, ?> args) {
                                        final Basic2DAgent agent = caller.agent();
                                        final Basic2DAgent clone;
                                        try {
                                            clone = objectPool.borrowObject(agent.getPopulation());
                                            agent.simulation().addAgent(clone, ImmutablePoint2D.at(0, 0));
                                        } catch (Exception e) {
                                            throw new AssertionError(e);
                                        }
                                        return null;
                                    }
                                })
                                .build(),
                        GenericAction.<Basic2DAgent>builder()
                                .name("die")
                                .executes(new Callback<GenericAction<Basic2DAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<Basic2DAgent> caller, final Map<String, ?> args) {
                                        final Basic2DAgent agent = caller.agent();
                                        agent.simulation().removeAgent(agent);

                                        try {
                                            objectPool.returnObject(agent.getPopulation(), agent);
                                        } catch (Exception e) {
                                            throw new AssertionError(e);
                                        }
                                        return null;
                                    }
                                })
                                .build())
                .build();

        agentMap.put(population, prototype);
        final DefaultBasic2DSimulation simulation = DefaultBasic2DSimulation
                .builder(space, prototype)
                .simulationLogger(SimulationLoggers.nullLogger())
                .build();
        for (int i = 0; i < populationSize; i++) {
            simulation.addAgent(CycleCloner.clone(prototype), ImmutablePoint2D.at(0, 0));
        }
        return simulation;
    }

    private Basic2DSimulation createSimulationWithoutObjectPool(final int populationSize) {
        final DefaultGreyfishTiled2DSpace space = DefaultGreyfishTiled2DSpaceImpl.ofSize(1, 1);
        final Population population = Population.named("test");
        final DefaultBasic2DAgent prototype = DefaultBasic2DAgent.builder(population)
                .addActions(
                        GenericAction.<Basic2DAgent>builder()
                                .name("reproduce")
                                .executedIf(GenericCondition.<Basic2DAgent>evaluate(Callbacks.random()))
                                .executes(new Callback<GenericAction<Basic2DAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<Basic2DAgent> caller, final Map<String, ?> args) {
                                        final Basic2DAgent agent = caller.agent();
                                        agent.simulation().addAgent(CycleCloner.clone(agent), ImmutablePoint2D.at(0, 0));
                                        return null;
                                    }
                                })
                                .build(),
                        GenericAction.<Basic2DAgent>builder()
                                .name("die")
                                .executes(new Callback<GenericAction<Basic2DAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<Basic2DAgent> caller, final Map<String, ?> args) {
                                        final Basic2DAgent agent = caller.agent();
                                        agent.simulation().removeAgent(agent);
                                        return null;
                                    }
                                })
                                .build())
                .build();

        final DefaultBasic2DSimulation simulation = DefaultBasic2DSimulation
                .builder(space, prototype)
                .simulationLogger(SimulationLoggers.nullLogger())
                .build();
        for (int i = 0; i < populationSize; i++) {
            simulation.addAgent(CycleCloner.clone(prototype), ImmutablePoint2D.at(0, 0));
        }
        return simulation;
    }
}
