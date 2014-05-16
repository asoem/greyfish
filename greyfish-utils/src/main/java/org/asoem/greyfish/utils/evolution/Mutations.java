package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSets;
import org.asoem.greyfish.utils.collect.BitString;
import org.asoem.greyfish.utils.math.statistics.Samplings;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
            final BitString positionTemplate = BitString.random(input.size(), rng, p);
            final BitString mutationTemplate = BitString.random(input.size(), rng, 0.5);

            return mutate(input, positionTemplate, mutationTemplate);
        }

        @VisibleForTesting
        static BitString mutate(final BitString input,
                                final BitString positionTemplate,
                                final BitString mutationTemplate) {
            final BitString flippedZeros = input.or(positionTemplate.and(mutationTemplate));
            return flippedZeros.and(positionTemplate.not().or(mutationTemplate));
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
            final BitSet inputAsSet = BitSets.create(input);
            for (Map.Entry<Integer, Boolean> mutation : mutationMap.entrySet()) {
                if (mutation.getValue()) {
                    inputAsSet.set(mutation.getKey(), mutation.getValue());
                }
            }
            return BitString.forBitSet(inputAsSet, input.size());
        }
    }
}
