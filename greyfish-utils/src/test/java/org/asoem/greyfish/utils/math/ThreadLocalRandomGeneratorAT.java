/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.math;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.asoem.greyfish.utils.collect.Tuple2;
import org.asoem.greyfish.utils.math.statistics.StatisticalTests;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

import static org.asoem.greyfish.utils.math.SignificanceLevel.HIGHLY_SIGNIFICANT;
import static org.asoem.greyfish.utils.math.SignificanceLevel.SIGNIFICANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ThreadLocalRandomGeneratorAT {
    private static final Logger logger = LoggerFactory.getLogger(ThreadLocalRandomGeneratorAT.class);

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
        final Tuple2<DescriptiveStatistics, DescriptiveStatistics> results =
                measure(nThreads, generatorSupplier, nMeasurements, taskFactory);

        // then
        assertThat(results.second().getMean(), is(lessThan(results.first().getMean())));

        assertThat("The mean elapsed time of the thread local generator" +
                "is not less than the mean elapsed time of the synchronized generator",
                results.second().getMean(), is(lessThan(results.first().getMean())));

        // Is it also significantly faster? Make a t-test.
        // Test assumptions for t-test: normality
        assertThat("Is not normal distributed", StatisticalTests.shapiroWilk(results.second().getValues()).p(), is(lessThan(SIGNIFICANT.getAlpha())));
        assertThat("Is not normal distributed", StatisticalTests.shapiroWilk(results.first().getValues()).p(), is(lessThan(SIGNIFICANT.getAlpha())));

        // Perform the t-test
        final double t = new TTest().t(results.second(), results.first());
        final double p = new TTest().tTest(results.second(), results.first());
        logger.info("t-test: t={}, p={}", t, p);
        double qt = new TDistribution(results.second().getN() - 1 + results.first().getN() - 1).inverseCumulativeProbability(1 - SIGNIFICANT.getAlpha() / 2);
        assertThat("The means are not significantly different", Math.abs(t), is(greaterThan(qt)));
    }

    @Test
    public void acceptSignificantBenefitWith10Threads() throws Exception {
        // given
        final int nThreads = 10;

        // when
        final Tuple2<DescriptiveStatistics, DescriptiveStatistics> results =
                measure(nThreads, generatorSupplier, nMeasurements, taskFactory);

        // then
        assertThat(results.second().getMean(), is(lessThan(results.first().getMean())));

        double p = new TTest().tTest(results.first(), results.second());
        System.out.println("t-Test: p=" + p);
        assertThat(p, is(lessThanOrEqualTo(HIGHLY_SIGNIFICANT.getAlpha())));
    }

    private static Tuple2<DescriptiveStatistics, DescriptiveStatistics> measure(final int nThreads, final Supplier<RandomGenerator> generatorSupplier, final int nMeasurements, final TaskFactory taskFactory) throws InterruptedException, ExecutionException {
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
        final DescriptiveStatistics statisticsSynchronized = new DescriptiveStatistics();
        for (Future<Long> longFuture : futuresSynchronized) {
            statisticsSynchronized.addValue(longFuture.get());
        }

        final List<Future<Long>> futuresThreadLocal = Lists.newArrayList();
        for (int i = 0; i < nMeasurements; i++) {
            final Callable<Long> task = taskFactory.createTask(threadLocalRandomGenerator);
            final Future<Long> future = executorService.submit(task);
            futuresThreadLocal.add(future);
        }
        final DescriptiveStatistics statisticsThreadLocal = new DescriptiveStatistics();
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
