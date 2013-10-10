package org.asoem.greyfish.utils.math;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class ThreadLocalRandomGeneratorAT {
    @Test
    public void testThreadLocalGeneratorPerformanceVsSynchronized() throws Exception {
        // given
        final RandomGenerator synchronizedRandomGenerator = RandomGenerators.synchronizedGenerator(new Well19937c());
        final RandomGenerator threadLocalRandomGenerator = RandomGenerators.threadLocalGenerator(new Supplier<RandomGenerator>() {
            @Override
            public RandomGenerator get() {
                return new Well19937c();
            }
        });

        // when
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        final List<Future<Long>> futuresSynchronized = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            final Callable<Long> task = measureBooleanGeneration(synchronizedRandomGenerator, 1000, TimeUnit.MICROSECONDS);
            final Future<Long> future = executorService.submit(task);
            futuresSynchronized.add(future);
        }
        final SummaryStatistics statisticsSynchronized = new SummaryStatistics();
        for (Future<Long> longFuture : futuresSynchronized) {
            statisticsSynchronized.addValue(longFuture.get());
        }

        final List<Future<Long>> futuresThreadLocal = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            final Callable<Long> task = measureBooleanGeneration(threadLocalRandomGenerator, 1000, TimeUnit.MICROSECONDS);
            final Future<Long> future = executorService.submit(task);
            futuresThreadLocal.add(future);
        }
        final SummaryStatistics statisticsThreadLocal = new SummaryStatistics();
        for (Future<Long> longFuture : futuresThreadLocal) {
            statisticsThreadLocal.addValue(longFuture.get());
        }

        // then
        System.out.println("ThreadLocal stats in us: " + statisticsThreadLocal);
        System.out.println("Synchronized stats in us: " + statisticsSynchronized);
        assertThat(statisticsThreadLocal.getMean(), is(lessThan(statisticsSynchronized.getMean())));

        double tTest = new TTest().tTest(statisticsSynchronized, statisticsThreadLocal);
        System.out.println("t-Test: p=" + tTest);
        assertThat(tTest, is(lessThan(0.5))); // minimum significance
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
}
