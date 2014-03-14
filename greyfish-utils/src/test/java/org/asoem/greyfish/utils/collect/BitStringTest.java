package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.math.SignificanceLevel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class BitStringTest {

    @Test
    public void testRandomSmallP() throws Exception {
        // given
        final int length = 100000;
        final double p = 1.0 / length;

        // when
        Map<Integer, Integer> samples1 = observedFrequencies(length, p, 1000); // cardinality of BitString.random()
        Map<Integer, Integer> samples2 = expectedFrequencies(length, p, 1000); // sample of binomial dist.

        // then
        assertThat(chiSquareTest(samples1, samples2), is(greaterThan(SignificanceLevel.SIGNIFICANT.getAlpha())));
    }

    @Test
    public void testRandomBigP() throws Exception {
        // given
        final int length = 1000;
        final double p = 0.5;

        // when
        Map<Integer, Integer> samples1 = observedFrequencies(length, p, 1000); // cardinality of BitString.random()
        Map<Integer, Integer> samples2 = expectedFrequencies(length, p, 1000); // sample of binomial dist.

        // then
        assertThat(chiSquareTest(samples1, samples2), is(greaterThan(SignificanceLevel.SIGNIFICANT.getAlpha())));
    }

    public static double chiSquareTest(final Map<Integer, Integer> samples1, final Map<Integer, Integer> samples2) {
        final ArrayList<Long> observed1 = Lists.newArrayList();
        final ArrayList<Long> observed2 = Lists.newArrayList();
        for (Integer key : Sets.union(samples1.keySet(), samples2.keySet())) {
            final Integer freq1 = Optional.fromNullable(samples1.get(key)).or(0);
            final Integer freq2 = Optional.fromNullable(samples2.get(key)).or(0);

            observed1.add(freq1.longValue());
            observed2.add(freq2.longValue());
        }

        return new ChiSquareTest().chiSquareTestDataSetsComparison(Longs.toArray(observed1), Longs.toArray(observed2));
    }

    public static Map<Integer, Integer> expectedFrequencies(final int length, final double p, final int sampleSize) {
        final Map<Integer, Integer> frequencies = Maps.newHashMap();
        final BinomialDistribution binomialDistribution = new BinomialDistribution(length, p);
        for (int i = 0; i < sampleSize; i++) {
            final int sample = binomialDistribution.sample();
            Integer integer = frequencies.get(sample);
            if (integer == null) {
                frequencies.put(sample, 0);
                integer = 0;
            }
            frequencies.put(sample, integer + 1);
        }
        return frequencies;
    }

    public static Map<Integer, Integer> observedFrequencies(final int length, final double p, final int sampleSize) {
        final Map<Integer, Integer> frequencies = Maps.newHashMap();
        for (int i = 0; i < sampleSize; i++) {
            final int cardinality = BitString.random(length, RandomGenerators.rng(), p).cardinality();
            Integer integer = frequencies.get(cardinality);
            if (integer == null) {
                frequencies.put(cardinality, 0);
                integer = 0;
            }
            frequencies.put(cardinality, integer + 1);
        }
        return frequencies;
    }
}
