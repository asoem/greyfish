package org.asoem.greyfish.utils.collect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.AbstractList;
import java.util.BitSet;

import static com.google.common.base.Preconditions.*;

/**
 * An immutable, finite and ordered sequence of binary values.
 */
@ThreadSafe
public abstract class BitString extends AbstractList<Boolean> {

    /**
     * Get the number of 1 bits in this string.
     *
     * @return the number of 1 bits
     */
    public abstract int cardinality();

    /**
     * Returns a new {@link java.util.BitSet} containing all the bits in this bit string, so that {@code
     * bitString.get(i) == bitSet.get(i)} for {@code 0 <= i < size()}
     *
     * @return a new {@code BitSet}
     */
    public abstract BitSet toBitSet();

    /**
     * Returns a new long array containing all the bits in this bit string.
     *
     * @return a long array containing a little-endian representation of all the bits in this bit string
     */
    public abstract long[] toLongArray();

    /**
     * Creates a new bit sequence by performing a logical <b>AND</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitString and(final BitString other) {
        if (this instanceof Zeros || other instanceof Zeros
                || size() == 0 || other.size() == 0) {
            return new Zeros(Math.max(size(), other.size()));
        }
        final BitSet bitSet = this.toBitSet();
        bitSet.and(other.toBitSet());
        return new RegularBitString(bitSet, Math.max(size(), other.size()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>OR</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitString or(final BitString other) {
        if (other.size() == 0 || this instanceof Ones && size() >= other.size()) {
            return this;
        }
        if (size() == 0 || other instanceof Ones && other.size() >= size()) {
            return other;
        }
        final BitSet bitSet = this.toBitSet();
        bitSet.or(other.toBitSet());
        return new RegularBitString(bitSet, Math.max(size(), other.size()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>XOR</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitString xor(final BitString other) {
        if (other.size() == 0 || other instanceof Zeros) {
            return this;
        }
        if (size() == 0 || this instanceof Zeros) {
            return other;
        }
        final BitSet bitSet = this.toBitSet();
        bitSet.xor(other.toBitSet());
        return new RegularBitString(bitSet, Math.max(size(), other.size()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>AND NOT</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public final BitString andNot(final BitString other) {
        final BitSet bitSet = this.toBitSet();
        bitSet.andNot(other.toBitSet());
        return new RegularBitString(bitSet, Math.max(size(), other.size()));
    }

    public final BitString subSequence(final int start, final int end) {
        checkPositionIndexes(start, end, size());
        if (start == end) {
            return emptyBitSequence();
        }
        return new BitStringView(this, start, end);
    }

    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            builder.append(get(i) ? '1' : '0');
        }
        return builder.reverse().toString();
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BitString)) {
            return false;
        }
        final BitString bitString = (BitString) obj;
        return Iterables.elementsEqual(this, bitString);
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

    private static BitString create(final BitSet bitSet, final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new RegularBitString(bitSet, length);
    }

    private static BitString create(final BitSet bitSet, final int length, final int cardinality) {
        if (length == 0) {
            return emptyBitSequence();
        } else if (cardinality == 0) {
            return zeros(length);
        } else if (cardinality == length) {
            return ones(length);
        } else {
            return new RegularBitString(bitSet, length, cardinality);
        }
    }

    public static BitString ones(final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new Ones(length);
    }

    public static BitString zeros(final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new Zeros(length);
    }

    public static BitString create(final Iterable<? extends Boolean> val) {
        checkNotNull(val);
        final BitSet bs = new BitSet();
        int idx = 0;
        int cardinality = 0;
        for (final boolean b : val) {
            bs.set(idx++, b);
            if (b) {
                ++cardinality;
            }
        }
        return create(bs, idx, cardinality);
    }

    public static BitString concat(final BitString sequence1,
                                   final BitString sequence2) {
        checkNotNull(sequence1);
        checkNotNull(sequence2);
        return new CombinedString(sequence1, sequence2);
    }

    /**
     * Create a new {@code BitString} from given string {@code s} of '0' and '1' chars
     *
     * @param s a string of '0' and '1' chars
     * @return a new BitString equal to the representation of {@code s}
     */
    public static BitString parse(final String s) {
        return new RegularBitString(s);
    }

    public static BitString random(final int length, final RandomGenerator rng) {
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
        return new RegularBitString(BitSet.valueOf(longs), length);
    }

    private static BitString emptyBitSequence() {
        return EmptyBitString.INSTANCE;
    }

    public static BitString random(final int length, final RandomGenerator rng, final double p) {
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
            for (int i = 0; i < length; ++i) {
                if (rng.nextDouble() < p) {
                    bs.set(i);
                }
            }
            return new RegularBitString(bs, length);
        }
    }

    public static BitString forBitSet(final BitSet bitSet, final int length) {
        return create(bitSet, length);
    }

    @VisibleForTesting
    static final class RegularBitString extends BitString {
        private final BitSet bitSet; // is mutable, so don't expose outside of class
        private final int length;
        private final Supplier<Integer> cardinalitySupplier;

        RegularBitString(final BitSet bitSet, final int length) {
            this(bitSet, length, Suppliers.memoize(new Supplier<Integer>() {
                @Override
                public Integer get() {
                    return bitSet.cardinality();
                }
            }));
        }

        RegularBitString(final String s) {
            this(BitSets.parse(s), s.length());
        }

        RegularBitString(final BitSet bitSet, final int length, final int cardinality) {
            this(bitSet, length, new Supplier<Integer>() {
                @Override
                public Integer get() {
                    return cardinality;
                }
            });
        }

        RegularBitString(final BitSet bitSet, final int length, final Supplier<Integer> cardinalitySupplier) {
            assert bitSet != null;
            assert bitSet.length() <= length : "Length of bitSet was > length: " + bitSet.length() + " > " + length;
            assert cardinalitySupplier != null;
            this.bitSet = bitSet;
            this.length = length;
            this.cardinalitySupplier = cardinalitySupplier;
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            return bitSet.get(index);
        }

        @Override
        public int cardinality() {
            return cardinalitySupplier.get();
        }

        public BitSet toBitSet() {
            return (BitSet) bitSet.clone();
        }

        @Override
        public long[] toLongArray() {
            return bitSet.toLongArray();
        }

        @Override
        public int size() {
            return length;
        }
    }

    @VisibleForTesting
    static final class BitStringView extends BitString {
        private final BitString bitString;
        private final int start;
        private final int end;
        private final Supplier<Integer> cardinalityMemoizer = Suppliers.memoize(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return computeCardinality();
            }
        });

        public BitStringView(final BitString bitString, final int start, final int end) {
            this.bitString = bitString;
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
        public BitSet toBitSet() {
            return bitString.toBitSet().get(start, end);
        }

        @Override
        public long[] toLongArray() {
            return toBitSet().toLongArray();
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            return bitString.get(start + index);
        }

        @Override
        public int size() {
            return end - start;
        }
    }

    private static class EmptyBitString extends BitString {
        public static final EmptyBitString INSTANCE = new EmptyBitString();

        private EmptyBitString() {
        }

        @Override
        public int cardinality() {
            return 0;
        }

        @Override
        public BitSet toBitSet() {
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
        public final int size() {
            return 0;
        }
    }

    @VisibleForTesting
    static final class Ones extends BitString {
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
        public BitSet toBitSet() {
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
        public int size() {
            return length;
        }
    }

    @VisibleForTesting
    static final class Zeros extends BitString {
        private final int length;

        public Zeros(final int length) {
            this.length = length;
        }

        @Override
        public int cardinality() {
            return 0;
        }

        @Override
        public BitSet toBitSet() {
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
        public int size() {
            return length;
        }
    }

    @VisibleForTesting
    static final class CombinedString extends BitString {
        private final BitString sequence1;
        private final BitString sequence2;

        public CombinedString(final BitString sequence1, final BitString sequence2) {

            this.sequence1 = sequence1;
            this.sequence2 = sequence2;
        }

        @Override
        public int cardinality() {
            return sequence1.cardinality() + sequence2.cardinality();
        }

        @Override
        public BitSet toBitSet() {
            final BitSet bitSet1 = sequence1.toBitSet();
            final BitSet bitSet2 = sequence2.toBitSet();
            for (int i = 0; i < bitSet2.length(); i++) {
                bitSet1.set(sequence1.size() + i, sequence2.get(i));
            }
            return bitSet1;
        }

        @Override
        public long[] toLongArray() {
            return toBitSet().toLongArray();
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
        public int size() {
            return sequence1.size() + sequence2.size();
        }
    }
}
