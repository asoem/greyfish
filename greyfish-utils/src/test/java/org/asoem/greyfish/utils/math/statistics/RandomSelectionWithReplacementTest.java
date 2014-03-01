package org.asoem.greyfish.utils.math.statistics;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class RandomSelectionWithReplacementTest {
    @Test(expected = IllegalArgumentException.class)
    public void testSampleEmptyCollection() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of();
        final int sampleSize = 5;
        final RandomGenerator rng = mock(RandomGenerator.class);
        final Samplings.RandomSelectionWithReplacement sampling =
                new Samplings.RandomSelectionWithReplacement(rng);

        // when
        sampling.sample(elements, sampleSize);

        // then
        fail();
    }

    @Test
    public void testSampleCollectionWithOneElement() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of(42);
        final RandomGenerator rng = mock(RandomGenerator.class);
        final int sampleSize = 5;
        final Samplings.RandomSelectionWithReplacement sampling =
                new Samplings.RandomSelectionWithReplacement(rng);

        // when
        final Iterable<Integer> sample = sampling.sample(elements, sampleSize);

        // then
        assertThat(sample, is(equalTo((Object) ImmutableList.of(42, 42, 42, 42, 42))));
        verifyZeroInteractions(rng);
    }

    @Test
    public void testSample() throws Exception {
        // given
        final RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(0);
        final Collection<Integer> elements = ImmutableList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final int sampleSize = 5;
        final Samplings.RandomSelectionWithReplacement sampling =
                new Samplings.RandomSelectionWithReplacement(rng);

        // when
        final Iterable<Integer> sample = sampling.sample(elements, sampleSize);

        // then
        assertThat(sample, contains(7, 2, 6, 5, 5));
    }
}
