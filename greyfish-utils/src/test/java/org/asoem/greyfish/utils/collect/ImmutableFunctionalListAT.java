package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.test.Statistics;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.asoem.greyfish.utils.math.SignificanceLevel.SIGNIFICANT;

abstract class ImmutableFunctionalListAT {
    protected static void executeRandomized(final int runs, final RandomGenerator rng, final Runnable runnable1, final Runnable runnable2) {
        for (int i = 0; i < runs; i++) {
            if (rng.nextBoolean()) {
                runnable1.run();
                runnable2.run();
            } else {
                runnable2.run();
                runnable1.run();
            }
        }
    }

    protected static <T> long measureFindFirstArray(final List<T> list, final Iterable<? extends Predicate<? super T>> predicates) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (Predicate<? super T> predicate : predicates) {
            Iterables.tryFind(list, predicate);
        }
        return stopwatch.elapsed(TimeUnit.MICROSECONDS);
    }

    protected static <T> long measureFindFirstUnrolled(final FunctionalList<T> list, final Iterable<? extends Predicate<? super T>> predicates) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (Predicate<? super T> predicate : predicates) {
            list.findFirst(predicate);
        }
        return stopwatch.elapsed(TimeUnit.MICROSECONDS);
    }

    protected void testFindFirst(final int runs, final List<String> controlList, final FunctionalList<String> functionalList, final Iterable<Predicate<String>> predicates) {
        // given
        final DescriptiveStatistics statisticsFunctional = new DescriptiveStatistics();
        final DescriptiveStatistics statisticsControl = new DescriptiveStatistics();

        final Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                statisticsControl.addValue(measureFindFirstArray(controlList, predicates));
            }
        };

        final Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                statisticsFunctional.addValue(measureFindFirstUnrolled(functionalList, predicates));
            }
        };

        // when
        executeRandomized(runs, RandomGenerators.rng(), runnable1, runnable2);

        // then
        Statistics.assertSignificantDecrease(statisticsControl, statisticsFunctional, SIGNIFICANT.getAlpha());
    }
}
