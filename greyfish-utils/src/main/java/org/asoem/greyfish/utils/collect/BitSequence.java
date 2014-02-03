package org.asoem.greyfish.utils.collect;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;
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

    public final BitSequence and(final BitSequence other) {
        final BitSet bitSet = this.asBitSet();
        bitSet.and(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
    }

    public final BitSequence or(final BitSequence other) {
        final BitSet bitSet = this.asBitSet();
        bitSet.or(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
    }

    public final BitSequence xor(final BitSequence other) {
        final BitSet bitSet = this.asBitSet();
        bitSet.xor(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
    }

    public final BitSequence andNot(final BitSequence other) {
        final BitSet bitSet = this.asBitSet();
        bitSet.andNot(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
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

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BitSequence)) {
            return false;
        }
        final BitSequence bitSequence = (BitSequence) obj;
        return Iterables.elementsEqual(this, bitSequence);
    }

    @Override
    public final int hashCode() {
        int hashCode = 1;
        for (Object o : this) {
            hashCode = 31 * hashCode + o.hashCode();

            hashCode = ~~hashCode;
            // needed to deal with GWT integer overflow
        }
        return hashCode;
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
        return forIterable(Iterables.concat(sequence2, sequence1));
    }

    /**
     * Create a new {@code BitSequence} from given string {@code s} of '0' and '1' chars
     *
     * @param s a string of '0' and '1' chars
     * @return a new BitSequence equal to the representation of {@code s}
     */
    public static BitSequence parse(final String s) {
        return new RegularBitSequence(s);
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

    static final class RegularBitSequence extends BitSequence {
        private final BitSet bitSet; // is mutable, so don't expose outside of class
        private final int length;
        private final Supplier<Integer> cardinalityMemoizer = Suppliers.memoize(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return bitSet.cardinality();
            }
        });

        RegularBitSequence(final BitSet bitSet, final int length) {
            assert bitSet != null;

            this.bitSet = bitSet;
            this.length = length;
        }

        RegularBitSequence(final String s) {
            this(BitSets.parse(s), s.length());
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
            return cardinalityMemoizer.get();
        }

        public BitSet asBitSet() {
            return (BitSet) bitSet.clone();
        }
    }

    static final class BitSequenceView extends BitSequence {
        private final BitSequence bitSequence;
        private final int start;
        private final int end;
        private final Supplier<Integer> cardinalityMemoizer = Suppliers.memoize(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return computeCardinality();
            }
        });

        public BitSequenceView(final BitSequence bitSequence, final int start, final int end) {
            this.bitSequence = bitSequence;
            this.start = start;
            this.end = end;
        }

        @Override
        public int cardinality() {
            return cardinalityMemoizer.get();
        }

        private int computeCardinality() {
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
