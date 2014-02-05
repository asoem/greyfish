package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.*;

import javax.annotation.Nullable;

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
            final BitSequence crossoverTemplate = BitSequence.random(bitSequence1.length(), rng, p);

            final Iterable<Product3<Boolean, Boolean, Boolean>> zip =
                    Products.zip(bitSequence1, bitSequence2, crossoverTemplate);
            final Iterable<Product2<Boolean, Boolean>> crossoverPairs =
                    // convert into a list to fix state
                    ImmutableList.copyOf(Iterables.transform(zip,
                            new Function<Product3<Boolean, Boolean, Boolean>, Product2<Boolean, Boolean>>() {
                                private boolean state = true;

                                @Nullable
                                @Override
                                public Product2<Boolean, Boolean> apply(
                                        final Product3<Boolean, Boolean, Boolean> input) {
                                    state ^= input.third();
                                    return state
                                            ? Tuple2.of(input.first(), input.second())
                                            : Tuple2.of(input.second(), input.first());
                                }
                            }));

            final Product2<Iterable<Boolean>, Iterable<Boolean>> unzippedCrossoverPairs =
                    Products.unzip(crossoverPairs);
            final BitSequence recombinedBitSequence1 = BitSequence.forIterable(unzippedCrossoverPairs.first());
            final BitSequence recombinedBitSequence2 = BitSequence.forIterable(unzippedCrossoverPairs.second());

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
            final BitSequence crossoverTemplate = BitSequence.random(bitSequence1.length(), rng, p);

            final Iterable<Product3<Boolean, Boolean, Boolean>> zip =
                    Products.zip(bitSequence1, bitSequence2, crossoverTemplate);
            final Iterable<Product2<Boolean, Boolean>> crossoverPairs =
                    Iterables.transform(zip,
                            new Function<Product3<Boolean, Boolean, Boolean>, Product2<Boolean, Boolean>>() {
                                @Nullable
                                @Override
                                public Product2<Boolean, Boolean> apply(
                                        final Product3<Boolean, Boolean, Boolean> input) {
                                    return input.third()
                                            ? Tuple2.of(input.first(), input.second())
                                            : Tuple2.of(input.second(), input.first());
                                }
                            });

            final Product2<Iterable<Boolean>, Iterable<Boolean>> unzippedCrossoverPairs =
                    Products.unzip(crossoverPairs);
            final BitSequence recombinedBitSequence1 = BitSequence.forIterable(unzippedCrossoverPairs.first());
            final BitSequence recombinedBitSequence2 = BitSequence.forIterable(unzippedCrossoverPairs.second());

            return RegularRecombinationProduct.of(recombinedBitSequence1, recombinedBitSequence2);
        }
    }
}
