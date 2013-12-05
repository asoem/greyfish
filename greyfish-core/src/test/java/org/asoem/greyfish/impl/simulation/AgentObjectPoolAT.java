package org.asoem.greyfish.impl.simulation;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolUtils;
import org.apache.commons.pool.impl.StackObjectPool;
import org.asoem.greyfish.core.actions.GenericAction;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.impl.agent.DefaultBasicAgent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
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

        final DefaultBasicAgent.Builder builder = DefaultBasicAgent.builder(PrototypeGroup.named(""))
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
                                .build());
        final DefaultBasicAgent agent = builder
                .build();

        final Supplier<BasicAgent> agentFactory = new Supplier<BasicAgent>() {

            @Override
            public BasicAgent get() {
                return builder.build();
            }
        };

        // when
        final int objects = 1000;
        for (int i = 0; i < runs; i++) {
            // randomize execution order
            if (RandomGenerators.rng().nextBoolean()) {
                statisticsWithoutObjectPool.addValue(measureAgentCreation(objects, agentFactory));
                statisticsWithObjectPool.addValue(measureAgentRecycling(objects, agentFactory));
            } else {
                statisticsWithObjectPool.addValue(measureAgentRecycling(objects, agentFactory));
                statisticsWithoutObjectPool.addValue(measureAgentCreation(objects, agentFactory));
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

    private double measureAgentRecycling(final int objects, final Supplier<BasicAgent> agent) throws Exception {
        final ObjectPool<BasicAgent> objectPool = PoolUtils.synchronizedPool(new StackObjectPool<>(new BasePoolableObjectFactory<BasicAgent>() {
            @Override
            public BasicAgent makeObject() throws Exception {
                return agent.get();
            }
        }, objects));

        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < objects; j++) {
            BasicAgent clone = objectPool.borrowObject();
            clone.initialize();
            objectPool.returnObject(clone);
        }
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private double measureAgentCreation(final int objects, final Supplier<BasicAgent> agentSupplier) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < objects; j++) {
            final BasicAgent clone = agentSupplier.get();
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
        final SimulationWithoutObjectPoolFactory simulationWithoutObjectPoolFactory = new SimulationWithoutObjectPoolFactory(populationSize, executorService);
        final SimulationWithObjectPoolFactory simulationWithObjectPoolFactory = new SimulationWithObjectPoolFactory(populationSize, executorService);

        // when
        for (int i = 0; i < runs; i++) {
            // randomize execution order
            if (RandomGenerators.rng().nextBoolean()) {
                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(simulationWithoutObjectPoolFactory.newSimulation(), steps));

                statisticsWithObjectPool.addValue(
                        measureExecutionTime(simulationWithObjectPoolFactory.newSimulation(), steps));
            } else {
                statisticsWithObjectPool.addValue(
                        measureExecutionTime(simulationWithObjectPoolFactory.newSimulation(), steps));

                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(simulationWithoutObjectPoolFactory.newSimulation(), steps));
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

    public static final class SimulationWithObjectPoolFactory {

        private final ObjectPool<BasicAgent> agentObjectPool;
        private final PrototypeGroup prototypeGroup = PrototypeGroup.named("test");
        private final DefaultBasicAgent.Builder builder = DefaultBasicAgent.builder(prototypeGroup)
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
                                            clone = agentObjectPool.borrowObject();
                                            agent.getContext().get().getSimulation().enqueueAddition(clone);
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
                                        final BasicSimulation simulation = agent.getContext().get().getSimulation();
                                        simulation.enqueueRemoval(agent, new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    // TODO: agent is not removed yet
                                                    agentObjectPool.returnObject(agent);
                                                } catch (Exception e) {
                                                    throw new AssertionError(e);
                                                }
                                            }
                                        }, executor);
                                        return null;
                                    }
                                })
                                .build());

        private final int populationSize;

        private final ExecutorService executorService;

        public SimulationWithObjectPoolFactory(final int populationSize, final ExecutorService executorService) {
            this.populationSize = populationSize;
            this.executorService = executorService;
            this.agentObjectPool = new StackObjectPool<>(new BasePoolableObjectFactory<BasicAgent>() {
                @Override
                public BasicAgent makeObject() throws Exception {
                    return builder.build();
                }

                @Override
                public void activateObject(final BasicAgent obj) throws Exception {
                    obj.initialize();
                }
            }, populationSize / 2);
        }

        public SynchronizedAgentsSimulation<?> newSimulation() {
            final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("TestSimulation")
                    .executorService(executorService)
                    .build();
            for (int i = 0; i < populationSize; i++) {
                final BasicAgent clone = builder.build();
                simulation.enqueueAddition(clone);
            }
            return simulation;
        }
    }

    private class SimulationWithoutObjectPoolFactory {
        private final int populationSize;
        private final ExecutorService executorService;
        final PrototypeGroup prototypeGroup = PrototypeGroup.named("test");
        final DefaultBasicAgent.Builder builder = DefaultBasicAgent.builder(prototypeGroup)
                .addAllActions(
                        GenericAction.<BasicAgent>builder()
                                .name("reproduce")
                                .executedIf(GenericCondition.<BasicAgent>evaluate(Callbacks.random()))
                                .executes(new Callback<GenericAction<BasicAgent>, Void>() {
                                    @Override
                                    public Void apply(final GenericAction<BasicAgent> caller, final Map<String, ?> args) {
                                        final BasicAgent agent = caller.agent().get();
                                        final BasicAgent clone = builder.build();
                                        clone.initialize();
                                        agent.getContext().get().getSimulation().enqueueAddition(clone);
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
                                        final BasicSimulation simulation = agent.getContext().get().getSimulation();
                                        simulation.enqueueRemoval(agent);
                                        return null;
                                    }
                                })
                                .build());

        public SimulationWithoutObjectPoolFactory(final int populationSize, final ExecutorService executorService) {
            this.populationSize = populationSize;
            this.executorService = executorService;
        }

        public SynchronizedAgentsSimulation<?> newSimulation() {
            final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("TestSimulation")
                    .executorService(executorService)
                    .build();
            for (int i = 0; i < populationSize; i++) {
                final BasicAgent clone = builder.build();
                simulation.enqueueAddition(clone);
            }
            return simulation;
        }
    }
}
