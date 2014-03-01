
package org.asoem.greyfish.utils.math.statistics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class RandomSelectionWithoutReplacementTest {
    @Test(expected = IllegalArgumentException.class)
    public void testSampleEmptyCollection() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of();
        final int sampleSize = 5;
        final RandomGenerator rng = mock(RandomGenerator.class);
        final Sampling<Object> sampling =
                new Samplings.RandomSelectionWithReplacement(rng);

        // when
        sampling.sample(elements, sampleSize);

        // then
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleWithInvalidSampleSize() throws Exception {
        // given
        final Collection<Integer> elements = ImmutableList.of(42);
        final RandomGenerator rng = mock(RandomGenerator.class);
        final int sampleSize = 5;
        final Sampling<Object> sampling =
                new Samplings.RandomSelectionWithoutReplacement(rng);

        // when
        sampling.sample(elements, sampleSize);

        // then
        fail();
    }

    @Test
    public void testSample() throws Exception {
        // given
        final RandomGenerator rng = new JDKRandomGenerator();
        rng.setSeed(0);
        final Collection<Integer> elements = ImmutableList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final int sampleSize = 5;
        final Sampling<Object> sampling =
                new Samplings.RandomSelectionWithoutReplacement(rng);

        // when
        final Iterable<Integer> sample = sampling.sample(elements, sampleSize);

        // then
        assertThat(sample, is(Matchers.<Integer>iterableWithSize(sampleSize)));
        assertThat("Sampled elements are not unique",
                Sets.newHashSet(sample), is(Matchers.<Integer>iterableWithSize(sampleSize)));
    }
}
