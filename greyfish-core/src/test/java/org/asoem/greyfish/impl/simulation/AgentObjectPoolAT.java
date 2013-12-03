package org.asoem.greyfish.impl.simulation;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.PoolUtils;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.actions.GenericAction;
import org.asoem.greyfish.core.agent.Agents;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.impl.agent.DefaultBasicAgent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

/**
 * Test whether we accept an object pool for agent recycling as an recommended strategy.
 */
public class AgentObjectPoolAT {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBasicSimulationTest.class);

    @Test
    public void testSignificantPerformanceBenefit1() throws Exception {
        // given
        final int populationSize = 400;
        final int steps = 1000;
        final int runs = 1000;
        final SummaryStatistics statisticsWithoutObjectPool = new SummaryStatistics();
        final SummaryStatistics statisticsWithObjectPool = new SummaryStatistics();

        final DefaultBasicAgent agent = DefaultBasicAgent.builder(PrototypeGroup.named(""))
                .addAllActions(
                        GenericAction.<BasicAgent>builder()
                                .name("reproduce")
                                .executedIf(AlwaysTrueCondition.<BasicAgent>builder().build())
                                .executes(Callbacks.emptyCallback())
                                .build(),
                        GenericAction.<BasicAgent>builder()
                                .name("die")
                                .executedIf(AlwaysTrueCondition.<BasicAgent>builder().build())
                                .executes(Callbacks.emptyCallback())
                                .build())
                .build();

        // when
        final int objects = 1000;
        for (int i = 0; i < runs; i++) {
            // randomize execution order
            if (RandomGenerators.rng().nextBoolean()) {
                statisticsWithoutObjectPool.addValue(measureAgentCreation(objects, agent));
                statisticsWithObjectPool.addValue(measureAgentRecycling(objects, agent));
            } else {
                statisticsWithObjectPool.addValue(measureAgentRecycling(objects, agent));
                statisticsWithoutObjectPool.addValue(measureAgentCreation(objects, agent));
            }
        }

        // then
        LOGGER.info("Simulation with object pool vs. without object pool: {}, {}",
                statisticsWithObjectPool, statisticsWithoutObjectPool);

        assertThat("The mean elapsed time of the getSimulation with an object pool " +
                "is not less than the mean elapsed time of the version with an object pool",
                statisticsWithObjectPool.getMean(), is(lessThan(statisticsWithoutObjectPool.getMean())));

        final double p = new TTest().tTest(statisticsWithObjectPool, statisticsWithoutObjectPool);
        LOGGER.info("t-test: p={}", p);
        assertThat("The measured difference is not significant", p, is(lessThan(0.05)));
    }

    private double measureAgentRecycling(final int objects, final BasicAgent agent) throws Exception {
        final Map<PrototypeGroup, BasicAgent> agentMap = ImmutableMap.of(agent.getPrototypeGroup(), agent);

        final KeyedObjectPool<PrototypeGroup, BasicAgent> objectPool = PoolUtils.synchronizedPool(new StackKeyedObjectPool<PrototypeGroup, BasicAgent>(new BaseKeyedPoolableObjectFactory<PrototypeGroup, BasicAgent>() {
            @Override
            public BasicAgent makeObject(final PrototypeGroup population) throws Exception {
                return CycleCloner.clone(agentMap.get(population));
            }
        }, objects) {

        });

        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < objects; j++) {
            BasicAgent clone = objectPool.borrowObject(agent.getPrototypeGroup());
            clone.initialize();
            objectPool.returnObject(agent.getPrototypeGroup(), clone);
        }
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private double measureAgentCreation(final int objects, final DefaultBasicAgent agent) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < objects; j++) {
            final BasicAgent clone = CycleCloner.clone(agent);
            clone.initialize();
        }
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSignificantPerformanceBenefit() throws Exception {
        // given
        final int populationSize = 400;
        final int steps = 1000;
        final int runs = 1000;
        final SummaryStatistics statisticsWithoutObjectPool = new SummaryStatistics();
        final SummaryStatistics statisticsWithObjectPool = new SummaryStatistics();

        final ExecutorService executorService = Executors.newFixedThreadPool(10);

        // when
        for (int i = 0; i < runs; i++) {
            // randomize execution order
            if (RandomGenerators.rng().nextBoolean()) {
                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(createSimulationWithoutObjectPool(populationSize, executorService), steps));

                statisticsWithObjectPool.addValue(
                        measureExecutionTime(createSimulationWithObjectPool(populationSize, executorService), steps));
            } else {
                statisticsWithObjectPool.addValue(
                        measureExecutionTime(createSimulationWithObjectPool(populationSize, executorService), steps));

                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(createSimulationWithoutObjectPool(populationSize, executorService), steps));
            }
        }

        // then
        LOGGER.info("Simulation with object pool vs. without object pool: {}, {}",
                statisticsWithObjectPool, statisticsWithoutObjectPool);

        assertThat("The mean elapsed time of the getSimulation with an object pool " +
                "is not less than the mean elapsed time of the version with an object pool",
                statisticsWithObjectPool.getMean(), is(lessThan(statisticsWithoutObjectPool.getMean())));

        final double p = new TTest().tTest(statisticsWithObjectPool, statisticsWithoutObjectPool);
        LOGGER.info("t-test: p={}", p);
        assertThat("The measured difference is not significant", p, is(lessThan(0.05)));
    }

    private long measureExecutionTime(final SynchronizedAgentsSimulation<?> simulationWithoutObjectPool, final int steps) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < steps; j++) {
            simulationWithoutObjectPool.nextStep();
        }
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private SynchronizedAgentsSimulation<?> createSimulationWithObjectPool(final int populationSize, final ExecutorService executorService) {

        final Map<PrototypeGroup, BasicAgent> agentMap = Maps.newHashMap();

        final KeyedObjectPool<PrototypeGroup, BasicAgent> objectPool = PoolUtils.synchronizedPool(new StackKeyedObjectPool<PrototypeGroup, BasicAgent>(new BaseKeyedPoolableObjectFactory<PrototypeGroup, BasicAgent>() {
            @Override
            public BasicAgent makeObject(final PrototypeGroup population) throws Exception {
                return CycleCloner.clone(agentMap.get(population));
            }

            @Override
            public void activateObject(final PrototypeGroup key, final BasicAgent obj) throws Exception {
                obj.initialize();
            }
        }, populationSize / 2) {

        });

        final PrototypeGroup prototypeGroup = PrototypeGroup.named("test");
        final DefaultBasicAgent prototype = DefaultBasicAgent.builder(prototypeGroup)
                .addAllActions(
                        GenericAction.<BasicAgent>builder()
                                .name("reproduce")
                                .executedIf(GenericCondition.<BasicAgent>evaluate(Callbacks.random()))
                                .executes(new Callback<GenericAction<BasicAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<BasicAgent> caller, final Map<String, ?> args) {
                                        final BasicAgent agent = caller.agent().get();
                                        final BasicAgent clone;
                                        try {
                                            clone = objectPool.borrowObject(agent.getPrototypeGroup());
                                            agent.simulation().enqueueAddition(clone);
                                        } catch (Exception e) {
                                            throw new AssertionError(e);
                                        }
                                        return null;
                                    }
                                })
                                .build(),
                        GenericAction.<BasicAgent>builder()
                                .name("die")
                                .executes(new Callback<GenericAction<BasicAgent>, Void>() {

                                    private ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();

                                    @Override
                                    public Void apply(final GenericAction<BasicAgent> caller, final Map<String, ?> args) {
                                        final BasicAgent agent = caller.agent().get();
                                        agent.simulation().enqueueRemoval(agent, new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    // TODO: agent is not removed yet
                                                    objectPool.returnObject(agent.getPrototypeGroup(), agent);
                                                } catch (Exception e) {
                                                    throw new AssertionError(e);
                                                }
                                            }
                                        }, executor);
                                        return null;
                                    }
                                })
                                .build())
                .build();

        agentMap.put(prototypeGroup, prototype);
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("TestSimulation")
                .executorService(executorService)
                .build();
        for (int i = 0; i < populationSize; i++) {
            final BasicAgent clone = Agents.<BasicAgent>createClone(prototype).build();
            simulation.enqueueAddition(clone);
        }
        return simulation;
    }

    private SynchronizedAgentsSimulation<?> createSimulationWithoutObjectPool(final int populationSize, final ExecutorService executorService) {
        final PrototypeGroup prototypeGroup = PrototypeGroup.named("test");
        final DefaultBasicAgent prototype = DefaultBasicAgent.builder(prototypeGroup)
                .addAllActions(
                        GenericAction.<BasicAgent>builder()
                                .name("reproduce")
                                .executedIf(GenericCondition.<BasicAgent>evaluate(Callbacks.random()))
                                .executes(new Callback<GenericAction<BasicAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<BasicAgent> caller, final Map<String, ?> args) {
                                        final BasicAgent agent = caller.agent().get();
                                        final BasicAgent clone = Agents.createClone(agent).build();
                                        clone.initialize();
                                        agent.simulation().enqueueAddition(clone);
                                        return null;
                                    }
                                })
                                .build(),
                        GenericAction.<BasicAgent>builder()
                                .name("die")
                                .executes(new Callback<GenericAction<BasicAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<BasicAgent> caller, final Map<String, ?> args) {
                                        final BasicAgent agent = caller.agent().get();
                                        agent.simulation().enqueueRemoval(agent);
                                        return null;
                                    }
                                })
                                .build())
                .build();

        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("TestSimulation")
                .executorService(executorService)
                .build();
        for (int i = 0; i < populationSize; i++) {
            final BasicAgent clone = Agents.<BasicAgent>createClone(prototype).build();
            simulation.enqueueAddition(clone);
        }
        return simulation;
    }
}
