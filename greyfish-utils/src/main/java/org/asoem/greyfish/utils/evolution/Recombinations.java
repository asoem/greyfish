package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSequence;

import java.util.BitSet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A utility class for creating recombination operators.
 */
public final class Recombinations {
    private Recombinations() {}

    /**
     * Create a n-point crossover recombination function for bit sequences of equal length. <p>The parameter n is not
     * explicitly given but a result of selecting each position before a bit as a crossover point with probability
     * {@code p}.</p>
     *
     * @param rng the random generator to use
     * @param p   the probability for any position in the bit sequence to become a crossover point
     * @return a new n-point crossover recombination function
     */
    public static Recombination<BitSequence> nPointCrossover(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        return new NPointCrossover(rng, p);
    }

    /**
     * Create an uniform crossover recombination function for bit sequences of equal length. This crossover technique
     * swaps the bits of the two parents at each position with probability {@code p}.
     *
     * @param rng the random generator to use
     * @param p   the probability to swap the bits of the parents at any given position
     * @return a new n-point crossover recombination function
     */
    public static Recombination<BitSequence> uniformCrossover(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        return new UniformCrossover(rng, p);
    }

    private static final class RegularRecombinationProduct<T> implements RecombinationProduct<T> {
        private final T sequence1;
        private final T sequence2;

        private RegularRecombinationProduct(final T sequence1, final T sequence2) {
            this.sequence1 = sequence1;
            this.sequence2 = sequence2;
        }

        public static <T> RecombinationProduct<T> of(final T sequence1, final T sequence2) {
            return new RegularRecombinationProduct<>(sequence1, sequence2);
        }

        @Override
        public T first() {
            return sequence1;
        }

        @Override
        public T second() {
            return sequence2;
        }
    }

    @VisibleForTesting
    static class NPointCrossover implements Recombination<BitSequence> {
        private final RandomGenerator rng;
        private final double p;

        public NPointCrossover(final RandomGenerator rng, final double p) {
            this.rng = rng;
            this.p = p;
        }

        @Override
        public RecombinationProduct<BitSequence> recombine(
                final BitSequence bitSequence1, final BitSequence bitSequence2) {
            checkNotNull(bitSequence1);
            checkNotNull(bitSequence2);
            checkArgument(bitSequence1.length() == bitSequence2.length());

            final int length = bitSequence1.length();
            final BitSequence crossoverTemplate = BitSequence.random(length, rng, p);

            final BitSet bitSet1 = BitSet.valueOf(bitSequence1.toLongArray());
            final BitSet bitSet2 = BitSet.valueOf(bitSequence2.toLongArray());

            boolean state = false;
            for (int i = 0; i < crossoverTemplate.length(); i++) {
                state ^= crossoverTemplate.get(i);
                if (state) {
                    bitSet1.set(i, bitSequence2.get(i));
                    bitSet2.set(i, bitSequence1.get(i));
                }
            }

            final BitSequence recombinedBitSequence1 = BitSequence.forBitSet(bitSet1, length);
            final BitSequence recombinedBitSequence2 = BitSequence.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitSequence1, recombinedBitSequence2);
        }
    }

    @VisibleForTesting
    static class UniformCrossover implements Recombination<BitSequence> {
        private final RandomGenerator rng;
        private final double p;

        public UniformCrossover(final RandomGenerator rng, final double p) {
            this.rng = rng;
            this.p = p;
        }

        @Override
        public RecombinationProduct<BitSequence> recombine(
                final BitSequence bitSequence1, final BitSequence bitSequence2) {
            checkNotNull(bitSequence1);
            checkNotNull(bitSequence2);
            checkArgument(bitSequence1.length() == bitSequence2.length());

            final int length = bitSequence1.length();
            final BitSequence crossoverTemplate = BitSequence.random(length, rng, p);

            final BitSet bitSet1 = BitSet.valueOf(bitSequence1.toLongArray());
            final BitSet bitSet2 = BitSet.valueOf(bitSequence2.toLongArray());

            for (int i = 0; i < crossoverTemplate.length(); i++) {
                final Boolean aBoolean = crossoverTemplate.get(i);

                if (aBoolean) {
                    bitSet1.set(i, bitSequence2.get(i));
                    bitSet2.set(i, bitSequence1.get(i));
                }
            }

            final BitSequence recombinedBitSequence1 = BitSequence.forBitSet(bitSet1, length);
            final BitSequence recombinedBitSequence2 = BitSequence.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitSequence1, recombinedBitSequence2);
        }
    }
}
