package org.asoem.greyfish.impl.simulation;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.asoem.greyfish.core.actions.GenericAction;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.impl.agent.DefaultBasicAgent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.collect.ConcurrentObjectPool;
import org.asoem.greyfish.utils.collect.LoadingObjectPool;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.math.statistics.StatisticalTests;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.asoem.greyfish.utils.math.SignificanceLevel.SIGNIFICANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test whether we accept an object pool for agent recycling as an recommended strategy.
 */
public class AgentObjectPoolAT {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBasicSimulationTest.class);

    @Test
    public void testSignificantPerformanceBenefit() throws Exception {
        // given
        final int runs = 1000;
        final DescriptiveStatistics statisticsWithoutObjectPool = new DescriptiveStatistics();
        final DescriptiveStatistics statisticsWithObjectPool = new DescriptiveStatistics();

        final Supplier<BasicAgent> agentFactory = new Supplier<BasicAgent>() {

            @Override
            public BasicAgent get() {
                return DefaultBasicAgent.builder(PrototypeGroup.named(""))
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
                                        .build()).build();
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
        logger.info("Simulation with object pool vs. without object pool: {}, {}",
                statisticsWithObjectPool, statisticsWithoutObjectPool);

        // Is it faster?
        assertThat("The mean elapsed time of the version with an object pool " +
                "is not less than the mean elapsed time of the version with an object pool",
                statisticsWithObjectPool.getMean(), is(lessThan(statisticsWithoutObjectPool.getMean())));

        // Is it also significantly faster? Make a t-test.
        // Test assumptions for t-test: normality
        assertThat("Is not normal distributed", StatisticalTests.shapiroWilk(statisticsWithObjectPool.getValues()).p(), is(lessThan(SIGNIFICANT.getAlpha())));
        assertThat("Is not normal distributed", StatisticalTests.shapiroWilk(statisticsWithoutObjectPool.getValues()).p(), is(lessThan(SIGNIFICANT.getAlpha())));

        // Perform the t-test
        final double t = new TTest().t(statisticsWithObjectPool, statisticsWithoutObjectPool);
        final double p = new TTest().tTest(statisticsWithObjectPool, statisticsWithoutObjectPool);
        logger.info("t-test: t={}, p={}", t, p);
        double qt = new TDistribution(statisticsWithObjectPool.getN() - 1 + statisticsWithoutObjectPool.getN() - 1).inverseCumulativeProbability(1 - SIGNIFICANT.getAlpha() / 2);
        assertThat("The means are not significantly different", Math.abs(t), is(greaterThan(qt)));
    }

    private double measureAgentRecycling(final int objects, final Supplier<BasicAgent> agentSupplier) throws Exception {
        LoadingObjectPool<BasicAgent> objectPool = ConcurrentObjectPool.create(new Callable<BasicAgent>() {
            @Override
            public BasicAgent call() throws Exception {
                return agentSupplier.get();
            }
        });

        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < objects; j++) {
            final BasicAgent clone = objectPool.borrow();
            clone.initialize();
            objectPool.release(clone);
        }
        return stopwatch.elapsed(TimeUnit.MICROSECONDS);
    }

    private double measureAgentCreation(final int objects, final Supplier<BasicAgent> agentSupplier) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < objects; j++) {
            final BasicAgent clone = agentSupplier.get();
            clone.initialize();
        }
        return stopwatch.elapsed(TimeUnit.MICROSECONDS);
    }

    @Test
    public void testSignificantPerformanceBenefitInSimulationContext() throws Exception {
        // given
        final int populationSize = 400;
        final int steps = 30000;
        final int runs = 20;
        final DescriptiveStatistics statisticsWithoutObjectPool = new DescriptiveStatistics();
        final DescriptiveStatistics statisticsWithObjectPool = new DescriptiveStatistics();

        final ExecutorService executorService = MoreExecutors.sameThreadExecutor();

        // when
        for (int i = 0; i < runs; i++) {
            // randomize execution order
            if (RandomGenerators.rng().nextBoolean()) {
                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(new SimulationWithoutObjectPoolFactory(populationSize, executorService).newSimulation(), steps));

                statisticsWithObjectPool.addValue(
                        measureExecutionTime(new SimulationWithObjectPoolFactory(populationSize, executorService).newSimulation(), steps));
            } else {
                statisticsWithObjectPool.addValue(
                        measureExecutionTime(new SimulationWithObjectPoolFactory(populationSize, executorService).newSimulation(), steps));

                statisticsWithoutObjectPool.addValue(
                        measureExecutionTime(new SimulationWithoutObjectPoolFactory(populationSize, executorService).newSimulation(), steps));
            }
        }

        // then
        logger.info("Simulation with object pool vs. without object pool: {}, {}",
                statisticsWithObjectPool, statisticsWithoutObjectPool);

        assertThat("The mean elapsed time of the version with an object pool " +
                "is not less than the mean elapsed time of the version with an object pool",
                statisticsWithObjectPool.getMean(), is(lessThan(statisticsWithoutObjectPool.getMean())));

        // Is it also significantly faster? Make a t-test.
        // Test assumptions for t-test: normality
        assertThat("Is not normal distributed", StatisticalTests.shapiroWilk(statisticsWithObjectPool.getValues()).p(), is(lessThan(0.05)));
        assertThat("Is not normal distributed", StatisticalTests.shapiroWilk(statisticsWithoutObjectPool.getValues()).p(), is(lessThan(0.05)));

        final double t = TestUtils.t(statisticsWithObjectPool, statisticsWithoutObjectPool);
        final double p = TestUtils.tTest(statisticsWithObjectPool, statisticsWithoutObjectPool);
        logger.info("t-test: t={}, p={}", t, p);
        double qt = new TDistribution(statisticsWithObjectPool.getN() - 1 + statisticsWithoutObjectPool.getN() - 1).inverseCumulativeProbability(0.975);
        assertThat("The means are not significantly different", Math.abs(t), is(greaterThan(qt)));
    }

    private long measureExecutionTime(final SynchronizedAgentsSimulation<?> simulationWithoutObjectPool, final int steps) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int j = 0; j < steps; j++) {
            simulationWithoutObjectPool.nextStep();
        }
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    public static final class SimulationWithObjectPoolFactory {

        private final LoadingObjectPool<BasicAgent> agentObjectPool;
        private final PrototypeGroup prototypeGroup = PrototypeGroup.named("test");
        private final int populationSize;
        private final ExecutorService executorService;

        public SimulationWithObjectPoolFactory(final int populationSize, final ExecutorService executorService) {
            this.populationSize = populationSize;
            this.executorService = executorService;
            this.agentObjectPool = ConcurrentObjectPool.create(new Callable<BasicAgent>() {
                @Override
                public BasicAgent call() throws Exception {
                    return createAgent();
                }
            });
        }

        public SynchronizedAgentsSimulation<?> newSimulation() {
            final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("TestSimulation")
                    .eventBus(new EventBus() {
                        @Override
                        public void post(final Object event) {
                            // NOP
                        }
                    })
                    .executorService(executorService)
                    .build();
            for (int i = 0; i < populationSize; i++) {
                final BasicAgent clone = createAgent();
                simulation.enqueueAddition(clone);
            }
            return simulation;
        }

        private DefaultBasicAgent createAgent() {
            return DefaultBasicAgent.builder(prototypeGroup)
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
                                                clone = agentObjectPool.borrow();
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
                                                        agentObjectPool.release(agent);
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
        }
    }

    private class SimulationWithoutObjectPoolFactory {
        private final int populationSize;
        private final ExecutorService executorService;
        private final PrototypeGroup prototypeGroup = PrototypeGroup.named("test");

        public SimulationWithoutObjectPoolFactory(final int populationSize, final ExecutorService executorService) {
            this.populationSize = populationSize;
            this.executorService = executorService;
        }

        public SynchronizedAgentsSimulation<?> newSimulation() {
            final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("TestSimulation")
                    .eventBus(new EventBus() {
                        @Override
                        public void post(final Object event) {
                            // NOP
                        }
                    })
                    .executorService(executorService)
                    .build();
            for (int i = 0; i < populationSize; i++) {
                final BasicAgent clone = createAgent();
                clone.initialize();
                simulation.enqueueAddition(clone);
            }
            return simulation;
        }

        private DefaultBasicAgent createAgent() {
            return DefaultBasicAgent.builder(prototypeGroup)
                    .addAllActions(
                            GenericAction.<BasicAgent>builder()
                                    .name("reproduce")
                                    .executedIf(GenericCondition.<BasicAgent>evaluate(Callbacks.random()))
                                    .executes(new Callback<GenericAction<BasicAgent>, Void>() {
                                        @Override
                                        public Void apply(final GenericAction<BasicAgent> caller, final Map<String, ?> args) {
                                            final BasicAgent agent = caller.agent().get();
                                            final BasicAgent clone = createAgent();
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
                                    .build())
                    .build();
        }
    }
}
