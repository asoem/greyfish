package org.asoem.greyfish.utils.math;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.asoem.greyfish.utils.collect.Tuple2;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.asoem.greyfish.utils.math.SignificanceLevel.HIGHLY_SIGNIFICANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ThreadLocalRandomGeneratorAT {

    private final TaskFactory taskFactory = new TaskFactory() {
        @Override
        Callable<Long> createTask(final RandomGenerator generator) {
            final int nIterations = 1000;
            return measureBooleanGeneration(generator, nIterations, TimeUnit.MICROSECONDS);
        }
    };
    private final int nMeasurements = 1000;
    private final Supplier<RandomGenerator> generatorSupplier = new Supplier<RandomGenerator>() {
        @Override
        public RandomGenerator get() {
            return new Well19937c();
        }
    };

    @Test
    public void acceptSignificantBenefitWith2Threads() throws Exception {
        // given
        final int nThreads = 2;

        // when
        final Tuple2<SummaryStatistics, SummaryStatistics> results =
                measure(nThreads, generatorSupplier, nMeasurements, taskFactory);

        // then
        assertThat(results._2().getMean(), is(lessThan(results._1().getMean())));

        double p = new TTest().tTest(results._1(), results._2());
        System.out.println("t-Test: p=" + p);
        assertThat(p, is(lessThanOrEqualTo(HIGHLY_SIGNIFICANT.getAlpha())));
    }

    @Test
    public void acceptSignificantBenefitWith10Threads() throws Exception {
        // given
        final int nThreads = 10;

        // when
        final Tuple2<SummaryStatistics, SummaryStatistics> results =
                measure(nThreads, generatorSupplier, nMeasurements, taskFactory);

        // then
        assertThat(results._2().getMean(), is(lessThan(results._1().getMean())));

        double p = new TTest().tTest(results._1(), results._2());
        System.out.println("t-Test: p=" + p);
        assertThat(p, is(lessThanOrEqualTo(HIGHLY_SIGNIFICANT.getAlpha())));
    }

    private static Tuple2<SummaryStatistics, SummaryStatistics> measure(final int nThreads, final Supplier<RandomGenerator> generatorSupplier, final int nMeasurements, final TaskFactory taskFactory) throws InterruptedException, ExecutionException {
        // given
        final RandomGenerator synchronizedRandomGenerator = RandomGenerators.synchronizedGenerator(generatorSupplier.get());
        final RandomGenerator threadLocalRandomGenerator = RandomGenerators.threadLocalGenerator(generatorSupplier);

        // when
        final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        final List<Future<Long>> futuresSynchronized = Lists.newArrayList();
        for (int i = 0; i < nMeasurements; i++) {
            final Callable<Long> task = taskFactory.createTask(synchronizedRandomGenerator);
            final Future<Long> future = executorService.submit(task);
            futuresSynchronized.add(future);
        }
        final SummaryStatistics statisticsSynchronized = new SummaryStatistics();
        for (Future<Long> longFuture : futuresSynchronized) {
            statisticsSynchronized.addValue(longFuture.get());
        }

        final List<Future<Long>> futuresThreadLocal = Lists.newArrayList();
        for (int i = 0; i < nMeasurements; i++) {
            final Callable<Long> task = taskFactory.createTask(threadLocalRandomGenerator);
            final Future<Long> future = executorService.submit(task);
            futuresThreadLocal.add(future);
        }
        final SummaryStatistics statisticsThreadLocal = new SummaryStatistics();
        for (Future<Long> longFuture : futuresThreadLocal) {
            statisticsThreadLocal.addValue(longFuture.get());
        }

        System.out.println("Synchronized stats in us: " + statisticsSynchronized);
        System.out.println("ThreadLocal stats in us: " + statisticsThreadLocal);

        return Tuple2.of(statisticsSynchronized, statisticsThreadLocal);
    }

    private static Callable<Long> measureBooleanGeneration(final RandomGenerator randomGenerator, final int iterations, final TimeUnit timeUnit) {
        return new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Stopwatch stopwatch = Stopwatch.createStarted();
                for (int i = 0; i < iterations; i++) {
                    randomGenerator.nextBoolean();
                }
                return stopwatch.elapsed(timeUnit);
            }
        };
    }

    private abstract class TaskFactory {
        abstract Callable<Long> createTask(RandomGenerator generator);
    }
}
