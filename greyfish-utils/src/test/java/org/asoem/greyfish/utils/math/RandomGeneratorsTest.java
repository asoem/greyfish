package org.asoem.greyfish.utils.math;

import com.google.common.base.Supplier;
import com.google.common.collect.*;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class RandomGeneratorsTest {
    @Test(expected = IllegalArgumentException.class)
    public void testSampleEmptyCollection() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of();
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        RandomGenerators.sample(elements, rng);

        // then
        fail();
    }

    @Test
    public void testSampleCollectionWithOneElement() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of(42);
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        final Integer sample = RandomGenerators.sample(elements, rng);

        // then
        assertThat(sample, is(equalTo(42)));
        verifyZeroInteractions(rng);
    }

    @Test
    public void testSampleCollectionWithNElements() throws Exception {
        // given
        final RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(0);
        final Collection<Integer> elements = ImmutableList.of(42, 4, 543, 65, 34, 2);

        // when
        final Integer sample = RandomGenerators.sample(elements, rng);

        // then
        assertThat(sample, is(equalTo(34)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleNEmptyCollection() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of();
        final int sampleSize = 5;
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        RandomGenerators.sample(elements, sampleSize, rng);

        // then
        fail();
    }

    @Test
    public void testSampleNCollectionWithOneElement() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of(42);
        final RandomGenerator rng = mock(RandomGenerator.class);
        final int sampleSize = 5;

        // when
        final Collection<Integer> sample = RandomGenerators.sample(elements, sampleSize, rng);

        // then
        assertThat(sample, is(equalTo((Object) ImmutableList.of(42, 42, 42, 42, 42))));
        verifyZeroInteractions(rng);
    }

    @Test
    public void testSampleNCollection() throws Exception {
        // given
        final RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(0);
        final Collection<Integer> elements = ImmutableList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final int sampleSize = 5;

        // when
        final Collection<Integer> sample = RandomGenerators.sample(elements, sampleSize, rng);

        // then
        assertThat(sample, contains(7, 2, 6, 5, 5));
    }

    @Test
    public void testSampleUnique() throws Exception {
        // given
        final ContiguousSet<Integer> integers =
                ContiguousSet.create(Range.closedOpen(0, 10), DiscreteDomain.integers());
        final RandomGenerator rng = RandomGenerators.rng();

        // when
        final Iterable<Integer> sampled = RandomGenerators.sampleUnique(integers, 3, rng);

        // then
        assertThat(sampled, is(Matchers.<Integer>iterableWithSize(3)));
        assertThat("Sampled elements are not unique",
                Sets.newHashSet(sampled), is(Matchers.<Integer>iterableWithSize(3)));
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
