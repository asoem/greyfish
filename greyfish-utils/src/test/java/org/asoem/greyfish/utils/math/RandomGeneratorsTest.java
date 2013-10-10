package org.asoem.greyfish.utils.math;

import com.google.common.base.Supplier;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 24.07.13
 * Time: 10:29
 */
public class RandomGeneratorsTest {
    @Test
    public void testSampleCollection() throws Exception {
        // given
        final RandomGenerator rng = RandomGenerators.rng(0);
        final Collection<Integer> elements = ImmutableList.of(0,1,2,3,4,5,6,7,8,9);
        final int sampleSize = 5;

        // when
        final Collection<Integer> sample = RandomGenerators.sample(rng, elements, sampleSize);

        // then
        assertThat(sample, contains(6, 1, 1, 5, 0));
    }

    @Test
    public void testSampleUnique() throws Exception {
        // given
        final RandomGenerator rng = RandomGenerators.rng();
        final Set<Integer> elements = ContiguousSet.create(Range.closed(0, 100), DiscreteDomain.integers());
        final int sampleSize = 50;

        // when
        final Set<Integer> sample = RandomGenerators.sampleUnique(rng, elements, sampleSize);

        // then
        assertThat(sample, hasSize(sampleSize));
    }

    @Test
    public void testThreadLocalGeneratorOneThread() {
        // given
        final Supplier generatorSupplier = mock(Supplier.class);
        final RandomGenerator randomGeneratorThread1 = mock(RandomGenerator.class);
        final RandomGenerator randomGeneratorThread2 = mock(RandomGenerator.class);
        given(generatorSupplier.get()).willReturn(randomGeneratorThread1, randomGeneratorThread2);
        final RandomGenerator threadLocalRandomGenerator = RandomGenerators.threadLocalGenerator(generatorSupplier);

        // when
        threadLocalRandomGenerator.nextBoolean();
        threadLocalRandomGenerator.nextBoolean();

        // then
        verify(randomGeneratorThread1, times(2)).nextBoolean();
        verify(randomGeneratorThread2, never()).nextBoolean();
    }

    @Test
    public void testThreadLocalGeneratorTwoThreads() throws ExecutionException, InterruptedException {
        // given
        final Supplier generatorSupplier = mock(Supplier.class);
        final RandomGenerator randomGeneratorThread1 = mock(RandomGenerator.class);
        final RandomGenerator randomGeneratorThread2 = mock(RandomGenerator.class);
        given(generatorSupplier.get()).willReturn(randomGeneratorThread1, randomGeneratorThread2);
        final RandomGenerator threadLocalRandomGenerator = RandomGenerators.threadLocalGenerator(generatorSupplier);

        // when
        Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return threadLocalRandomGenerator.nextBoolean();
            }
        }).get();
        threadLocalRandomGenerator.nextBoolean();

        // then
        verify(randomGeneratorThread1).nextBoolean();
        verify(randomGeneratorThread2).nextBoolean();
    }
}
