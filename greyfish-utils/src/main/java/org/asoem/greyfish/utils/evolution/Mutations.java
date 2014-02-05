package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSequence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Mutations {
    private Mutations() {}

    public static Mutation<BitSequence> bitFlipMutation(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        return new BitFlipMutation(rng, p);
    }

    @VisibleForTesting
    static class BitFlipMutation implements Mutation<BitSequence> {
        private final RandomGenerator rng;
        private final double p;

        public BitFlipMutation(final RandomGenerator rng, final double p) {
            this.rng = rng;
            this.p = p;
        }

        @Override
        public BitSequence mutate(final BitSequence input) {
            checkNotNull(input);
            return input.xor(BitSequence.random(input.length(), rng, p));
        }
    }
}
