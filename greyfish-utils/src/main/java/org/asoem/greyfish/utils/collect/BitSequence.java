package org.asoem.greyfish.utils.collect;

import com.google.common.collect.Iterables;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.concurrent.ThreadSafe;
import java.util.BitSet;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.math.RandomGenerators.nextBoolean;

/**
 * An immutable linear sequence of boolean values.
 */
@ThreadSafe
public abstract class BitSequence extends AbstractLinearSequence<Boolean> {

    public abstract int cardinality();

    public abstract BitSet asBitSet();

    public final BitSequence and(final BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.and(this.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(length(), bs.length()));
    }

    public final BitSequence or(final BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.or(this.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(length(), bs.length()));
    }

    public final BitSequence xor(final BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.xor(this.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(length(), bs.length()));
    }

    public final BitSequence andNot(final BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.andNot(this.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(length(), bs.length()));
    }

    public final BitSequence subSequence(final int start, final int end) {
        return new BitSequenceView(this, start, end);
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.length(); i++) {
            builder.append(get(i) ? '1' : '0');
        }
        return builder.reverse().toString();
    }

    private static BitSequence create(final int length, final BitSet bitSet) {
        return new RegularBitSequence(bitSet, length);
    }

    public static BitSequence ones(final int length) {
        return create(length, BitSets.newBitSet(length, true));
    }

    public static BitSequence zeros(final int length) {
        return create(length, BitSets.newBitSet(length, false));
    }

    public static BitSequence forIterable(final Iterable<? extends Boolean> val) {
        checkNotNull(val);
        final BitSet bs = new BitSet();
        int idx = 0;
        for (final boolean b : val) {
            bs.set(idx++, b);
        }
        return create(idx, bs);
    }

    public static BitSequence concat(final Iterable<? extends Boolean> sequence1,
                                     final Iterable<? extends Boolean> sequence2) {
        checkNotNull(sequence1);
        checkNotNull(sequence2);
        return forIterable(Iterables.concat(sequence1, sequence2));
    }

    /**
     * Create a new {@code BitSequence} from given string {@code s} of '0' and '1' chars
     *
     * @param s a string of '0' and '1' chars
     * @return a new BitSequence equal to the representation of {@code s}
     */
    public static BitSequence parse(final String s) {
        return new RegularBitSequence(BitSets.parse(s), s.length());
    }

    public static BitSequence random(final int length, final RandomGenerator rng) {
        checkNotNull(rng);
        final BitSet bs = new BitSet(length);
        int idx = 0;
        for (int i = 0; i < length; ++i) {
            bs.set(idx++, rng.nextBoolean());
        }
        return new RegularBitSequence(bs, length);
    }

    public static BitSequence random(final int length, final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        final BitSet bs = new BitSet(length);
        int idx = 0;
        for (int i = 0; i < length; ++i) {
            bs.set(idx++, nextBoolean(rng, p));
        }
        return new RegularBitSequence(bs, length);
    }

    private static final class RegularBitSequence extends BitSequence {
        private final BitSet bitSet; // is mutable, so don't expose outside of class
        private final int length;

        private RegularBitSequence(final BitSet bitSet, final int length) {
            assert bitSet != null;

            this.bitSet = bitSet;
            this.length = length;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, length());
            return bitSet.get(index);
        }

        @Override
        public int cardinality() {
            return bitSet.cardinality();
        }

        public BitSet asBitSet() {
            return (BitSet) bitSet.clone();
        }
    }

    private static final class BitSequenceView extends BitSequence {
        private final BitSequence bitSequence;
        private final int start;
        private final int end;

        public BitSequenceView(final BitSequence bitSequence, final int start, final int end) {
            super();
            this.bitSequence = bitSequence;
            this.start = start;
            this.end = end;
        }

        @Override
        public int cardinality() {
            int cardinality = 0;
            for (Boolean aBoolean : this) {
                cardinality += aBoolean ? 1 : 0;
            }
            return cardinality;
        }

        @Override
        public BitSet asBitSet() {
            return bitSequence.asBitSet().get(start, end);
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            return bitSequence.get(start + index);
        }

        @Override
        public int length() {
            return end - start;
        }
    }
}
