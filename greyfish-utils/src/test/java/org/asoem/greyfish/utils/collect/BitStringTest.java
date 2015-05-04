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

package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.math.SignificanceLevel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BitStringTest {

    @Test
    public void testHammingDistance() throws Exception {
        // given
        final BitString bitString1 = BitString.parse("00101001");
        final BitString bitString2 = BitString.parse("11101101");

        // when
        final int hammingDistance = bitString1.hammingDistance(bitString2);

        // then
        assertThat(hammingDistance, is(equalTo(3)));
    }

    @Test
    public void testRandomP1() throws Exception {
        // given
        final int length = 100;
        final RandomGenerator rng = RandomGenerators.rng(0);
        final int p = 1;

        // when
        final BitString random = BitString.random(length, rng, p);

        // then
        assertThat(random.cardinality(), is(length));
    }

    @Test
    public void testRandomP0() throws Exception {
        // given
        final int length = 100;
        final RandomGenerator rng = RandomGenerators.rng(0);
        final int p = 0;

        // when
        final BitString random = BitString.random(length, rng, p);

        // then
        assertThat(random.cardinality(), is(0));
    }

    @Test
    public void testRandomSmallP() throws Exception {
        // given
        final int length = 10;
        final double p = 1e-5;
        final RandomGenerator rng = RandomGenerators.rng(0);

        // when
        final Map<Integer, Integer> observedCardinalities = observedFrequencies(length, p, 100000, rng); // cardinality of BitString.random()
        final Map<Integer, Integer> observedBinomialSamples = expectedFrequencies(length, p, 100000, rng); // sample of binomial dist.

        // then
        assertThat(chiSquareTest(observedCardinalities, observedBinomialSamples),
                is(greaterThan(SignificanceLevel.SIGNIFICANT.getAlpha())));
    }

    @Test
    public void testRandomBigP() throws Exception {
        // given
        final int length = 1000;
        final double p = 0.5;
        final RandomGenerator rng = RandomGenerators.rng(0);

        // when
        final Map<Integer, Integer> observedCardinalities = observedFrequencies(length, p, 1000, rng); // cardinality of BitString.random()
        final Map<Integer, Integer> observedBinomialSamples = expectedFrequencies(length, p, 1000, rng); // sample of binomial dist.

        // then
        assertThat(chiSquareTest(observedCardinalities, observedBinomialSamples),
                is(greaterThan(SignificanceLevel.SIGNIFICANT.getAlpha())));
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

    public static Map<Integer, Integer> expectedFrequencies(final int length, final double p, final int sampleSize,
                                                            final RandomGenerator rng) {
        final Map<Integer, Integer> frequencies = Maps.newHashMap();
        final BinomialDistribution binomialDistribution = new BinomialDistribution(rng, length, p);
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

    public static Map<Integer, Integer> observedFrequencies(final int length, final double p, final int sampleSize,
                                                            final RandomGenerator rng) {
        final Map<Integer, Integer> frequencies = Maps.newHashMap();
        for (int i = 0; i < sampleSize; i++) {
            final int cardinality = BitString.random(length, rng, p).cardinality();
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
