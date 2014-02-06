package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSequence;

import java.util.BitSet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Recombinations {
    private Recombinations() {}

    public static Recombination<BitSequence> nPointCrossover(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        return new NPointCrossover(rng, p);
    }

    public static Recombination<BitSequence> uniformCrossover(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        return new UniformCrossover(rng, p);
    }

    private static final class RegularRecombinationProduct<T> implements RecombinationProduct<T> {
        private final T sequence1;
        private final T sequence2;

        public RegularRecombinationProduct(final T sequence1, final T sequence2) {
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
            final int length = bitSequence1.length();
            final BitSequence crossoverTemplate = BitSequence.random(length, rng, p);

            final BitSet bitSet1 = new BitSet(length);
            final BitSet bitSet2 = new BitSet(length);

            boolean state = true;
            for (int i = 0; i < crossoverTemplate.length(); i++) {
                state ^= crossoverTemplate.get(i);
                if (state) {
                    bitSet1.set(i);
                } else {
                    bitSet2.set(i);
                }
            }

            final BitSequence recombinedBitSequence1 = BitSequence.forBitSet(bitSet1, length);
            final BitSequence recombinedBitSequence2 = BitSequence.forBitSet(bitSet1, length);

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
            final int length = bitSequence1.length();
            final BitSequence crossoverTemplate = BitSequence.random(length, rng, p);

            final BitSet bitSet1 = new BitSet(length);
            final BitSet bitSet2 = new BitSet(length);

            for (int i = 0; i < crossoverTemplate.length(); i++) {
                final Boolean aBoolean = crossoverTemplate.get(i);
                if (aBoolean) {
                    bitSet1.set(i);
                } else {
                    bitSet2.set(i);
                }
            }

            final BitSequence recombinedBitSequence1 = BitSequence.forBitSet(bitSet1, length);
            final BitSequence recombinedBitSequence2 = BitSequence.forBitSet(bitSet1, length);

            return RegularRecombinationProduct.of(recombinedBitSequence1, recombinedBitSequence2);
        }
    }
}
