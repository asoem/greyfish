package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
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
     * Create a n-point crossover recombination function for bit sequences of equal length.
     *
     * @param rng the random generator to use
     * @param n   the number of random unique crossover points
     * @return a new n-point crossover recombination function
     */
    public static Recombination<BitString> nPointCrossover(final RandomGenerator rng, final int n) {
        checkNotNull(rng);
        checkArgument(n >= 0);
        return new NPointCrossover(n, rng);
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
        private final Function<? super Integer, ? extends BitString> crossoverPointSampling;

        @VisibleForTesting
        NPointCrossover(final int n, final RandomGenerator rng) {
            this(new Function<Integer, BitString>() {
                @Override
                public BitString apply(final Integer input) {
                    final UniformIntegerDistribution indexDistribution =
                            new UniformIntegerDistribution(rng, 0, input - 1);

                    final BitSet bitSet = new BitSet();
                    int cardinality = 0;
                    while (cardinality != n) {
                        final int index = indexDistribution.sample();
                        final boolean b = bitSet.get(index);
                        if (!b) {
                            bitSet.set(index);
                            ++cardinality;
                        }
                    }
                    return BitString.forBitSet(bitSet, input);
                }
            });
        }

        @VisibleForTesting
        NPointCrossover(final Function<? super Integer, ? extends BitString> crossoverPointSampling) {
            this.crossoverPointSampling = checkNotNull(crossoverPointSampling);
        }

        @Override
        public RecombinationProduct<BitString> recombine(
                final BitString bitString1, final BitString bitString2) {
            checkNotNull(bitString1);
            checkNotNull(bitString2);
            checkArgument(bitString1.size() == bitString2.size());

            final int length = bitString1.size();

            final BitSet bitSet1 = BitSet.valueOf(bitString1.toLongArray());
            final BitSet bitSet2 = BitSet.valueOf(bitString2.toLongArray());

            boolean state = false;

            final BitString crossoverPoints = checkNotNull(crossoverPointSampling.apply(length));

            int lastCrossoverPoint = 0;
            for (int i = crossoverPoints.nextSetBit(0); i >= 0; i = crossoverPoints.nextSetBit(i + 1)) {
                if (state) {
                    copyRange(bitSet1, bitString2, lastCrossoverPoint, i);
                    copyRange(bitSet2, bitString1, lastCrossoverPoint, i);
                }
                state = !state;
                lastCrossoverPoint = i;
            }
            if (state) {
                copyRange(bitSet1, bitString2, lastCrossoverPoint, length);
                copyRange(bitSet2, bitString1, lastCrossoverPoint, length);
            }

            final BitString recombinedBitString1 = BitString.forBitSet(bitSet1, length);
            final BitString recombinedBitString2 = BitString.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitString1, recombinedBitString2);
        }

        private void copyRange(final BitSet bitSet1, final BitString bitString2,
                               final int from, final int to) {
            for (int i = from; i < to; i++) {
                bitSet1.set(i, bitString2.get(i));
            }
        }
    }

    @VisibleForTesting
    static class UniformCrossover implements Recombination<BitString> {
        private final Function<? super Integer, ? extends BitString> crossoverPointSampling;

        @VisibleForTesting
        UniformCrossover(final RandomGenerator rng, final double p) {
            this(new Function<Integer, BitString>() {
                @Override
                public BitString apply(final Integer input) {
                    return BitString.random(input, rng, p);
                }
            });
        }

        @VisibleForTesting
        UniformCrossover(final Function<? super Integer, ? extends BitString> crossoverPointSampling) {
            this.crossoverPointSampling = checkNotNull(crossoverPointSampling);
        }

        @Override
        public RecombinationProduct<BitString> recombine(
                final BitString bitString1, final BitString bitString2) {
            checkNotNull(bitString1);
            checkNotNull(bitString2);
            checkArgument(bitString1.size() == bitString2.size());

            final int length = bitString1.size();

            final BitSet bitSet1 = BitSet.valueOf(bitString1.toLongArray());
            final BitSet bitSet2 = BitSet.valueOf(bitString2.toLongArray());

            final BitString crossoverPoints = checkNotNull(crossoverPointSampling.apply(length));

            for (int i = crossoverPoints.nextSetBit(0); i >= 0; i = crossoverPoints.nextSetBit(i + 1)) {
                bitSet1.set(i, bitString2.get(i));
                bitSet2.set(i, bitString1.get(i));
            }

            final BitString recombinedBitString1 = BitString.forBitSet(bitSet1, length);
            final BitString recombinedBitString2 = BitString.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitString1, recombinedBitString2);
        }
    }
}
