package org.asoem.greyfish.utils.collect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.BitSet;

import static com.google.common.base.Preconditions.*;

/**
 * An immutable linear sequence of boolean values.
 */
@ThreadSafe
public abstract class BitSequence extends AbstractLinearSequence<Boolean> {

    public abstract int cardinality();

    public abstract BitSet asBitSet();

    /**
     * Creates a new bit sequence by performing a logical <b>AND</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitSequence and(final BitSequence other) {
        if (this instanceof Zeros || other instanceof Zeros
                || this.length() == 0 || other.length() == 0) {
            return new Zeros(Math.max(this.length(), other.length()));
        }
        final BitSet bitSet = this.asBitSet();
        bitSet.and(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>OR</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitSequence or(final BitSequence other) {
        if (other.length() == 0 || this instanceof Ones && this.length() >= other.length()) {
            return this;
        }
        if (this.length() == 0 || other instanceof Ones && other.length() >= this.length()) {
            return other;
        }
        final BitSet bitSet = this.asBitSet();
        bitSet.or(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>XOR</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitSequence xor(final BitSequence other) {
        if (other.length() == 0 || other instanceof Zeros) {
            return this;
        }
        if (this.length() == 0 || this instanceof Zeros) {
            return other;
        }
        final BitSet bitSet = this.asBitSet();
        bitSet.xor(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>AND NOT</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitSequence andNot(final BitSequence other) {
        final BitSet bitSet = this.asBitSet();
        bitSet.andNot(other.asBitSet());
        return new RegularBitSequence(bitSet, Math.max(this.length(), other.length()));
    }

    public final BitSequence subSequence(final int start, final int end) {
        checkPositionIndexes(start, end, size());
        if (start == end) {
            return emptyBitSequence();
        }
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

    public abstract long[] toLongArray();

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
        if (length == 0) {
            return emptyBitSequence();
        }
        return new RegularBitSequence(bitSet, length);
    }

    public static BitSequence ones(final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new Ones(length);
    }

    public static BitSequence zeros(final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new Zeros(length);
    }

    public static BitSequence forIterable(final Iterable<? extends Boolean> val) {
        checkNotNull(val);
        final BitSet bs = new BitSet();
        int idx = 0;
        for (final boolean b : val) {
            // TODO: we could record the cardinality here
            bs.set(idx++, b);
        }
        if (idx == 0) {
            return emptyBitSequence();
        }
        return create(idx, bs);
    }

    public static BitSequence concat(final BitSequence sequence1,
                                     final BitSequence sequence2) {
        checkNotNull(sequence1);
        checkNotNull(sequence2);
        return new CombinedSequence(sequence1, sequence2);
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
        checkArgument(length >= 0);

        if (length == 0) {
            return emptyBitSequence();
        }

        long[] longs = new long[(length + 63) / 64];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = rng.nextLong();
        }
        longs[longs.length - 1] = longs[longs.length - 1] & (0xffffffffffffffffL >>> (longs.length * 64 - length));
        return new RegularBitSequence(BitSet.valueOf(longs), length);
    }

    private static BitSequence emptyBitSequence() {
        return EmptyBitSequence.INSTANCE;
    }

    public static BitSequence random(final int length, final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        checkArgument(length >= 0);

        if (length == 0) {
            return emptyBitSequence();
        }

        if (p == 0) {
            return zeros(length);
        } else if (p == 1) {
            return ones(length);
        } else if (p == 0.5) {
            return random(length, rng);
        } else {
            final BitSet bs = new BitSet(length);
            int idx = 0;
            for (int i = 0; i < length; ++i) {
                bs.set(idx++, rng.nextDouble() < p);
            }
            return new RegularBitSequence(bs, length);
        }
    }

    public static BitSequence forBitSet(final BitSet bitSet, final int length) {
        return create(length, bitSet);
    }

    @VisibleForTesting
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
            assert bitSet.length() <= length : "Length of bitSet was > length: " + bitSet.length() + " > " + length;
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

        @Override
        public long[] toLongArray() {
            return bitSet.toLongArray();
        }
    }

    @VisibleForTesting
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
        public long[] toLongArray() {
            return asBitSet().toLongArray();
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

    private static class EmptyBitSequence extends BitSequence {
        public static final EmptyBitSequence INSTANCE = new EmptyBitSequence();

        private EmptyBitSequence() {
        }

        @Override
        public int cardinality() {
            return 0;
        }

        @Override
        public BitSet asBitSet() {
            return new BitSet(0);
        }

        @Override
        public long[] toLongArray() {
            return new long[0];
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            throw new AssertionError("Unreachable");
        }

        @Override
        public int length() {
            return 0;
        }
    }

    @VisibleForTesting
    static final class Ones extends BitSequence {
        private final int length;

        public Ones(final int length) {
            assert length > 0;
            this.length = length;
        }

        @Override
        public int cardinality() {
            return length;
        }

        @Override
        public BitSet asBitSet() {
            return BitSet.valueOf(toLongArray());
        }

        @Override
        public long[] toLongArray() {
            long[] longs = new long[(length + 63) / 64];
            for (int i = 0; i < longs.length; i++) {
                longs[i] = ~longs[i];
            }
            longs[longs.length - 1] = longs[longs.length - 1] & (0xffffffffffffffffL >>> (longs.length * 64 - length));
            return longs;
        }

        @Override
        public Boolean get(final int index) {
            checkPositionIndex(index, size());
            return true;
        }

        @Override
        public int length() {
            return length;
        }
    }

    @VisibleForTesting
    static final class Zeros extends BitSequence {
        private final int length;

        public Zeros(final int length) {
            this.length = length;
        }

        @Override
        public int cardinality() {
            return 0;
        }

        @Override
        public BitSet asBitSet() {
            return new BitSet(length);
        }

        @Override
        public long[] toLongArray() {
            return new long[(length + 63) / 64];
        }

        @Override
        public Boolean get(final int index) {
            checkPositionIndex(index, size());
            return false;
        }

        @Override
        public int length() {
            return length;
        }
    }

    @VisibleForTesting
    static final class CombinedSequence extends BitSequence {
        private final BitSequence sequence1;
        private final BitSequence sequence2;

        public CombinedSequence(final BitSequence sequence1, final BitSequence sequence2) {

            this.sequence1 = sequence1;
            this.sequence2 = sequence2;
        }

        @Override
        public int cardinality() {
            return sequence1.cardinality() + sequence2.cardinality();
        }

        @Override
        public BitSet asBitSet() {
            final BitSet bitSet1 = sequence1.asBitSet();
            final BitSet bitSet2 = sequence2.asBitSet();
            for (int i = 0; i < bitSet2.length(); i++) {
                bitSet1.set(sequence1.size() + i, sequence2.get(i));
            }
            return bitSet1;
        }

        @Override
        public long[] toLongArray() {
            return asBitSet().toLongArray();
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            if (index < sequence1.size()) {
                return sequence1.get(index);
            } else {
                return sequence2.get((index - sequence1.size()));
            }
        }

        @Override
        public int length() {
            return sequence1.size() + sequence2.size();
        }
    }
}
