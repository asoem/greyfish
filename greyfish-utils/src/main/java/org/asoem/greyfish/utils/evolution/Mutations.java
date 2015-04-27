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

package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSets;
import org.asoem.greyfish.utils.collect.BitString;
import org.asoem.greyfish.utils.math.statistics.Samplings;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * A utility class for accessing different implementations of {@link org.asoem.greyfish.utils.evolution.Mutation}.
 */
public final class Mutations {
    private Mutations() {}

    /**
     * Create a new bit-flip mutation function. This function flips every bit in a given bit string with probability
     * {@code p}.
     *
     * @param rng the random generator to use.
     * @param p   the probability to flip a bit
     * @return a new flip-bit mutation function
     */
    public static Mutation<BitString> bitFlipMutation(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        return new BitFlipMutation(rng, p);
    }

    @VisibleForTesting
    static class BitFlipMutation implements Mutation<BitString> {
        private final RandomGenerator rng;
        private final double p;

        @VisibleForTesting
        BitFlipMutation(final RandomGenerator rng, final double p) {
            checkNotNull(rng);
            checkArgument(p >= 0 && p <= 1);

            this.rng = rng;
            this.p = p;
        }

        @Override
        public BitString mutate(final BitString input) {
            checkNotNull(input);
            final BitString flipTemplate = BitString.random(input.size(), rng, p);
            return mutate(input, flipTemplate);
        }

        @VisibleForTesting
        static BitString mutate(final BitString input, final BitString flipTemplate) {
            return input.xor(flipTemplate);
        }
    }

    /**
     * Create a new mutation function which sets each bit to a random value with probability {@code p}.
     *
     * @param rng the random generator to use.
     * @param p   the number of mutations
     * @return a new flip-bit mutation function
     */
    public static Mutation<BitString> randomMutation(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0);
        return new RandomMutation(rng, p);
    }

    @VisibleForTesting
    static class RandomMutation implements Mutation<BitString> {
        private final RandomGenerator rng;
        private final double p;

        @VisibleForTesting
        RandomMutation(final RandomGenerator rng, final double p) {
            checkNotNull(rng);
            checkArgument(p >= 0 && p <= 1);

            this.rng = rng;
            this.p = p;
        }

        @Override
        public BitString mutate(final BitString input) {
            checkNotNull(input);

            final BinomialDistribution binomialDistribution =
                    new BinomialDistribution(rng, input.size(), p);
            final int n = binomialDistribution.sample();

            return nPointRandom(rng, n).mutate(input);
        }
    }

    /**
     * Create a new n-point random mutation function. This function sets the bits of input strings at {@code n} random
     * positions to a random value.
     *
     * @param rng the random generator to use.
     * @param n   the number of mutations
     * @return a new flip-bit mutation function
     */
    public static Mutation<BitString> nPointRandom(final RandomGenerator rng, final int n) {
        checkNotNull(rng);
        checkArgument(n >= 0);
        return new NPointRandom(rng, n);
    }

    @VisibleForTesting
    static class NPointRandom implements Mutation<BitString> {
        private final RandomGenerator rng;
        private final int n;

        @VisibleForTesting
        NPointRandom(final RandomGenerator rng, final int n) {
            checkNotNull(rng);

            this.rng = rng;
            this.n = n;
        }

        @Override
        public BitString mutate(final BitString input) {
            checkNotNull(input);
            checkArgument(input.size() >= n);
            final ImmutableMap<Integer, Boolean> mutationMap = createMutationMap(input.size());
            return mutate(input, mutationMap);
        }

        @VisibleForTesting
        ImmutableMap<Integer, Boolean> createMutationMap(final int size) {
            final ContiguousSet<Integer> integers =
                    ContiguousSet.create(Range.closedOpen(0, size), DiscreteDomain.integers());
            final Iterable<Integer> indices = Samplings.random(rng).withoutReplacement().sample(integers, n);

            return Maps.toMap(indices, new Function<Integer, Boolean>() {
                @Nullable
                @Override
                public Boolean apply(@Nullable final Integer input) {
                    return rng.nextBoolean();
                }
            });
        }

        @VisibleForTesting
        static BitString mutate(final BitString input, final Map<Integer, Boolean> mutationMap) {
            if (mutationMap.isEmpty()) {
                return input;
            }

            final BitSet inputAsSet = BitSets.create(input);
            for (Map.Entry<Integer, Boolean> mutation : mutationMap.entrySet()) {
                final Integer index = mutation.getKey();
                final Boolean value = mutation.getValue();

                checkElementIndex(index, input.size());

                inputAsSet.set(index, value);
            }
            return BitString.forBitSet(inputAsSet, input.size());
        }
    }
}
