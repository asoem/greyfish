package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitString;

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
    public static Recombination<BitString> nPointCrossover(final RandomGenerator rng, final double p) {
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
    public static Recombination<BitString> uniformCrossover(final RandomGenerator rng, final double p) {
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
    static class NPointCrossover implements Recombination<BitString> {
        private final RandomGenerator rng;
        private final double p;

        public NPointCrossover(final RandomGenerator rng, final double p) {
            this.rng = rng;
            this.p = p;
        }

        @Override
        public RecombinationProduct<BitString> recombine(
                final BitString bitString1, final BitString bitString2) {
            checkNotNull(bitString1);
            checkNotNull(bitString2);
            checkArgument(bitString1.size() == bitString2.size());

            final int length = bitString1.size();
            final BitString crossoverTemplate = BitString.random(length, rng, p);

            final BitSet bitSet1 = BitSet.valueOf(bitString1.toLongArray());
            final BitSet bitSet2 = BitSet.valueOf(bitString2.toLongArray());

            boolean state = false;
            for (int i = 0; i < crossoverTemplate.size(); i++) {
                state ^= crossoverTemplate.get(i);
                if (state) {
                    bitSet1.set(i, bitString2.get(i));
                    bitSet2.set(i, bitString1.get(i));
                }
            }

            final BitString recombinedBitString1 = BitString.forBitSet(bitSet1, length);
            final BitString recombinedBitString2 = BitString.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitString1, recombinedBitString2);
        }
    }

    @VisibleForTesting
    static class UniformCrossover implements Recombination<BitString> {
        private final RandomGenerator rng;
        private final double p;

        public UniformCrossover(final RandomGenerator rng, final double p) {
            this.rng = rng;
            this.p = p;
        }

        @Override
        public RecombinationProduct<BitString> recombine(
                final BitString bitString1, final BitString bitString2) {
            checkNotNull(bitString1);
            checkNotNull(bitString2);
            checkArgument(bitString1.size() == bitString2.size());

            final int length = bitString1.size();
            final BitString crossoverTemplate = BitString.random(length, rng, p);

            final BitSet bitSet1 = BitSet.valueOf(bitString1.toLongArray());
            final BitSet bitSet2 = BitSet.valueOf(bitString2.toLongArray());

            for (int i = 0; i < crossoverTemplate.size(); i++) {
                final Boolean aBoolean = crossoverTemplate.get(i);

                if (aBoolean) {
                    bitSet1.set(i, bitString2.get(i));
                    bitSet2.set(i, bitString1.get(i));
                }
            }

            final BitString recombinedBitString1 = BitString.forBitSet(bitSet1, length);
            final BitString recombinedBitString2 = BitString.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitString1, recombinedBitString2);
        }
    }
}
