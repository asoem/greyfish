package org.asoem.greyfish.utils.math;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
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
        final RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(0);
        final Collection<Integer> elements = ImmutableList.of(0,1,2,3,4,5,6,7,8,9);
        final int sampleSize = 5;

        // when
        final Collection<Integer> sample = RandomGenerators.sample(rng, elements, sampleSize);

        // then
        assertThat(sample, contains(7, 2, 6, 5, 5));
    }

    @Test
    public void testSampleUnique() throws Exception {
        // given
        final int collectionSize = 10;
        final int sampleSize = 3;

        final List<Double> elementsToSample = Lists.newArrayList();
        for (int i = 0; i < collectionSize; i++) {
            elementsToSample.add(new Random().nextDouble());
        }

        final RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(0);

        // when
        final Iterable<Double> samples = RandomGenerators.sampleOnce(rng, elementsToSample, sampleSize);

        // then
        final ImmutableList<Double> actualSamples = ImmutableList.copyOf(samples);
        final ImmutableList<Double> expectedSamples = ImmutableList.of(
                elementsToSample.get(7),
                elementsToSample.get(2),
                elementsToSample.get(6));
        System.out.println(Joiner.on(",").join(elementsToSample));
        assertThat(actualSamples, Matchers.<Collection<Double>>both(Matchers.hasSize(expectedSamples.size()))
                .and(everyItem(isIn(expectedSamples))));
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
