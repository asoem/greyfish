package org.asoem.greyfish.utils.evolution;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitString;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Mutations {
    private Mutations() {}

    public static Mutation<BitString> bitFlipMutation(final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        return new BitFlipMutation(rng, p);
    }

    @VisibleForTesting
    static class BitFlipMutation implements Mutation<BitString> {
        private final RandomGenerator rng;
        private final double p;

        public BitFlipMutation(final RandomGenerator rng, final double p) {
            this.rng = rng;
            this.p = p;
        }

        @Override
        public BitString mutate(final BitString input) {
            checkNotNull(input);
            return input.xor(BitString.random(input.size(), rng, p));
        }
    }
}
