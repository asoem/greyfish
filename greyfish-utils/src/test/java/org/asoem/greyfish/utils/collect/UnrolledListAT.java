package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.test.Statistics;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.asoem.greyfish.utils.math.SignificanceLevel.SIGNIFICANT;

abstract class UnrolledListAT {
    protected static void executeRandomized(
            final int runs, final RandomGenerator rng, final Runnable runnable1, final Runnable runnable2) {
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

    protected static <T> long measureFindFirstIterative(
            final List<T> list, final Iterable<? extends Predicate<? super T>> predicates) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (Predicate<? super T> predicate : predicates) {
            findFirstIterative(list, predicate);
        }
        return stopwatch.elapsed(TimeUnit.MICROSECONDS);
    }

    private static <T> Optional<T> findFirstIterative(final List<T> list, final Predicate<? super T> predicate) {
        for (T t : list) {
            if (predicate.apply(t)) {
                return Optional.of(t);
            }
        }
        return Optional.absent();
    }

    protected static <T> long measureFindFirstUnrolled(
            final FunctionalList<T> list, final Iterable<? extends Predicate<? super T>> predicates) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (Predicate<? super T> predicate : predicates) {
            list.findFirst(predicate);
        }
        return stopwatch.elapsed(TimeUnit.MICROSECONDS);
    }

    protected static void testFindFirst(
            final int runs, final List<String> controlList, final FunctionalList<String> functionalList,
            final Iterable<Predicate<String>> predicates) {
        // given
        final DescriptiveStatistics statisticsFunctional = new DescriptiveStatistics();
        final DescriptiveStatistics statisticsControl = new DescriptiveStatistics();

        final Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                statisticsControl.addValue(measureFindFirstIterative(controlList, predicates));
            }
        };

        final Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                statisticsFunctional.addValue(measureFindFirstUnrolled(functionalList, predicates));
            }
        };

        // burn in phase
        executeRandomized(1000, RandomGenerators.rng(),
                new Runnable() {
                    @Override
                    public void run() {
                        measureFindFirstIterative(controlList, predicates);
                    }
                },

                new Runnable() {
                    @Override
                    public void run() {
                        measureFindFirstUnrolled(functionalList, predicates);
                    }
                }
        );

        // when
        executeRandomized(runs, RandomGenerators.rng(), runnable1, runnable2);

        // then
        Statistics.assertSignificantDecrease(statisticsControl, statisticsFunctional, SIGNIFICANT.getAlpha());
    }
}
