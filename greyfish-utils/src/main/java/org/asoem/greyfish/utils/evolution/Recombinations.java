package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSets;
import org.asoem.greyfish.utils.collect.BitString;
import org.asoem.greyfish.utils.math.statistics.Samplings;

import javax.annotation.Nullable;
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
        private final Function<? super Integer, ? extends Iterable<Integer>> crossoverPointSampling;

        @VisibleForTesting
        NPointCrossover(final int n, final RandomGenerator rng) {
            this(new Function<Integer, Iterable<Integer>>() {
                @Override
                public Iterable<Integer> apply(final Integer bitStringLength) {
                    return Samplings.random(rng).withoutReplacement().sample(ContiguousSet.create(Range.closedOpen(0, bitStringLength), DiscreteDomain.integers()), n
                    );
                }
            });
        }

        @VisibleForTesting
        NPointCrossover(final Function<? super Integer, ? extends Iterable<Integer>> crossoverPointSampling) {
            this.crossoverPointSampling = checkNotNull(crossoverPointSampling);
        }

        @Override
        public RecombinationProduct<BitString> recombine(
                final BitString bitString1, final BitString bitString2) {
            checkNotNull(bitString1);
            checkNotNull(bitString2);
            checkArgument(bitString1.size() == bitString2.size());

            final int length = bitString1.size();

            final BitSet bitSet1 = BitSets.create(bitString1);
            final BitSet bitSet2 = BitSets.create(bitString2);

            boolean state = false;

            final Iterable<Integer> crossoverPoints = checkNotNull(crossoverPointSampling.apply(length));

            int lastCrossoverPoint = 0;
            for (Integer i : crossoverPoints) {
                if (state) {
                    BitSets.swap(bitSet1, lastCrossoverPoint, bitSet2, lastCrossoverPoint, i - lastCrossoverPoint);
                }
                state = !state;
                lastCrossoverPoint = i;
            }
            if (state) {
                BitSets.swap(bitSet1, lastCrossoverPoint, bitSet2, lastCrossoverPoint, length - lastCrossoverPoint);
            }

            final BitString recombinedBitString1 = BitString.forBitSet(bitSet1, length);
            final BitString recombinedBitString2 = BitString.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitString1, recombinedBitString2);
        }
    }

    @VisibleForTesting
    static class UniformCrossover implements Recombination<BitString> {
        private final Function<? super Integer, ? extends Iterable<Integer>> crossoverPointSampling;

        @VisibleForTesting
        UniformCrossover(final RandomGenerator rng, final double p) {
            this(new Function<Integer, Iterable<Integer>>() {
                @Override
                public Iterable<Integer> apply(final Integer bitStringLength) {
                    if (bitStringLength * p < 0.01) {
                        return Samplings.random(rng).withoutReplacement().sample(ContiguousSet.create(Range.closedOpen(0, bitStringLength), DiscreteDomain.integers()), new BinomialDistribution(rng, bitStringLength, p).sample()
                        );
                    } else {
                        return Iterables.filter(
                                ContiguousSet.create(Range.closedOpen(0, bitStringLength), DiscreteDomain.integers()),
                                new Predicate<Integer>() {
                                    @Override
                                    public boolean apply(@Nullable final Integer input) {
                                        return p > rng.nextFloat();
                                    }
                                });
                    }
                }
            });
        }

        @VisibleForTesting
        UniformCrossover(final Function<? super Integer, ? extends Iterable<Integer>> crossoverPointSampling) {
            this.crossoverPointSampling = checkNotNull(crossoverPointSampling);
        }

        @Override
        public RecombinationProduct<BitString> recombine(
                final BitString bitString1, final BitString bitString2) {
            checkNotNull(bitString1);
            checkNotNull(bitString2);
            checkArgument(bitString1.size() == bitString2.size());

            final int length = bitString1.size();

            final BitSet bitSet1 = BitSets.create(bitString1);
            final BitSet bitSet2 = BitSets.create(bitString2);

            final Iterable<Integer> crossoverPoints = checkNotNull(crossoverPointSampling.apply(length));

            for (Integer i : crossoverPoints) {
                BitSets.swap(bitSet1, i, bitSet2, i, 1);
            }

            final BitString recombinedBitString1 = BitString.forBitSet(bitSet1, length);
            final BitString recombinedBitString2 = BitString.forBitSet(bitSet2, length);

            return RegularRecombinationProduct.of(recombinedBitString1, recombinedBitString2);
        }
    }
}
