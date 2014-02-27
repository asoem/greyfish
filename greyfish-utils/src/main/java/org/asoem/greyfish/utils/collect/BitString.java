package org.asoem.greyfish.utils.collect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.*;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.*;
import com.google.common.primitives.Longs;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.AbstractList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

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
     * Returns the bit set backend for this bit string.
     *
     * @return a {@code BitSet}
     */
    protected abstract BitSet bitSet();

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
    public abstract BitString and(BitString other);

    protected final BitString standardAnd(final BitString other) {
        final BitSet bitSetCopy = this.bitSet();
        bitSetCopy.and(other.bitSet());
        return BitString.create(bitSetCopy, Math.max(size(), other.size()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>OR</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public abstract BitString or(BitString other);

    protected final BitString standardOr(final BitString other) {
        final BitSet bitSetCopy = this.bitSet();
        bitSetCopy.or(other.bitSet());
        return BitString.create(bitSetCopy, Math.max(size(), other.size()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>XOR</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public abstract BitString xor(BitString other);

    protected final BitString standardXor(final BitString other) {
        final BitSet bitSetCopy = this.bitSet();
        bitSetCopy.xor(other.bitSet());
        return BitString.create(bitSetCopy, Math.max(size(), other.size()));
    }

    /**
     * Creates a new bit sequence by performing a logical <b>AND NOT</b> of this bit sequence with an {@code other} bit
     * sequence. If the lengths do differ, the smaller one is padded with zeros.
     *
     * @param other the other bit sequence
     * @return a new bit sequence
     */
    public abstract BitString andNot(BitString other);

    protected final BitString standardAndNot(final BitString other) {
        final BitSet bitSetCopy = this.bitSet();
        bitSetCopy.andNot(other.bitSet());
        return BitString.create(bitSetCopy, Math.max(size(), other.size()));
    }

    /**
     * Return the inverse of this bit string.
     *
     * @return the inverse of this bit string
     */
    public final BitString not() {
        if (this instanceof InverseString) {
            return ((InverseString) this).delegate;
        }
        return new InverseString(this);
    }

    /**
     * Create a bit string which is a view upon this string from start (inclusive) to end (exclusive).
     *
     * @param start the position in this string which should be the first in the new string
     * @param end   the position in this string which is the first not to get included in the new string
     * @return a view upon the current string
     */
    public final BitString subSequence(final int start, final int end) {
        checkPositionIndexes(start, end, size());
        if (start == end) {
            return emptyBitSequence();
        }
        return new SubString(start, end - start);
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
        return this.size() == bitString.size()
                && Iterables.elementsEqual(this.asIndices(), bitString.asIndices());
    }

    @Override
    public final int hashCode() {
        int hashCode = 1;
        for (Object o : this.asIndices()) {
            hashCode = 31 * hashCode + o.hashCode();

            hashCode = ~~hashCode;
            // needed to deal with GWT integer overflow
        }
        hashCode = 31 * hashCode + size();
        return hashCode;
    }

    private static BitString create(final BitSet bitSet, final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new BitSetString(bitSet, length);
    }

    private static BitString create(final BitSet bitSet, final int length, final int cardinality) {
        if (length == 0) {
            return emptyBitSequence();
        } else if (cardinality == 0) {
            return zeros(length);
        } else if (cardinality == length) {
            return ones(length);
        } else {
            return new BitSetString((BitSet) bitSet.clone(), length, cardinality);
        }
    }

    public static BitString ones(final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new InverseString(zeros(length));
    }

    public static BitString zeros(final int length) {
        if (length == 0) {
            return emptyBitSequence();
        }
        return new IndexSetString(ImmutableSet.<Integer>of(), length);
    }

    public static BitString create(final Iterable<Boolean> val) {
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
        return new BitSetString(BitSets.parse(s), s.length());
    }

    /**
     * Create a random bit string of given {@code length}.
     *
     * @param length the length of the bit string
     * @param rng    the random number generator to use
     * @return a new bit string of given length.
     */
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
        longs[longs.length - 1] &= (~0L >>> (longs.length * 64 - length));
        return new BitSetString(BitSet.valueOf(longs), length);
    }

    private static BitString emptyBitSequence() {
        return EmptyString.INSTANCE;
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
        } else {
            final BinomialDistribution binomialDistribution = new BinomialDistribution(rng, length, p);
            final int n = binomialDistribution.sample();

            if ((double) n / length < 1.0 / 32) { // < 1 bit per word?
                final ContiguousSet<Integer> indexRange =
                        ContiguousSet.create(Range.closedOpen(0, length), DiscreteDomain.integers());
                final Iterable<Integer> uniqueIndexSample = Samplings.randomWithoutReplacement(rng).sample(indexRange, n);
                return new IndexSetString(ImmutableSortedSet.copyOf(uniqueIndexSample), length);
            } else {
                final BitSet bs = new BitSet(length);
                for (int i = 0; i < length; i++) {
                    if (p < rng.nextFloat()) {
                        bs.set(i);
                    }
                }
                return new BitSetString(bs, length);
            }
        }
    }

    public static BitString forBitSet(final BitSet bitSet, final int length) {
        return create(bitSet, length);
    }

    public static BitString create(final int length, final long... longs) {
        return create(BitSet.valueOf(longs), length);
    }

    public static BitString forIndices(final Iterable<Integer> indices, final int length) {
        final Set<Integer> indexSet = ImmutableSortedSet.copyOf(indices);
        if ((double) indexSet.size() / length < 1.0 / 32) {
            return new IndexSetString(indexSet, length);
        } else {
            final BitSet bitSet = new BitSet(length);
            for (Integer index : indexSet) {
                bitSet.set(index);
            }
            return forBitSet(bitSet, length);
        }
    }

    /**
     * Create an iterable of ordered indices where this string's bits are set to 1.
     *
     * @return iterable of indices
     */
    public abstract Iterable<Integer> asIndices();

    @VisibleForTesting
    static final class BitSetString extends RandomAccessBitString {
        private final BitSet bitSet; // is mutable, so don't expose outside of class
        private final int length;
        private final Supplier<Integer> cardinalitySupplier;

        BitSetString(final BitSet bitSet, final int length) {
            this(bitSet, length, Suppliers.memoize(new Supplier<Integer>() {
                @Override
                public Integer get() {
                    return bitSet.cardinality();
                }
            }));
        }

        private BitSetString(final BitSet bitSet, final int length, final int cardinality) {
            this(bitSet, length, new Supplier<Integer>() {
                @Override
                public Integer get() {
                    return cardinality;
                }
            });
        }

        private BitSetString(final BitSet bitSet, final int length, final Supplier<Integer> cardinalitySupplier) {
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

        protected BitSet bitSet() {
            return (BitSet) bitSet.clone();
        }

        @Override
        public long[] toLongArray() {
            return bitSet().toLongArray();
        }

        @Override
        public int nextSetBit(final int from) {
            return bitSet.nextSetBit(from);
        }

        @Override
        public int size() {
            return length;
        }

        public BitString and(final BitString other) {
            return standardAnd(other);
        }

        public BitString or(final BitString other) {
            return standardOr(other);
        }

        public BitString xor(final BitString other) {
            return standardXor(other);
        }

        public BitString andNot(final BitString other) {
            return standardAndNot(other);
        }
    }

    @VisibleForTesting
    final class SubString extends BitString {
        private final int offset;
        private final int length;
        private final Supplier<Integer> cardinalityMemoizer = Suppliers.memoize(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return computeCardinality();
            }
        });

        public SubString(final int offset, final int length) {
            this.offset = offset;
            this.length = length;
        }

        @Override
        public int cardinality() {
            return cardinalityMemoizer.get();
        }

        private int computeCardinality() {
            return Iterables.size(asIndices());
        }

        @Override
        protected BitSet bitSet() {
            return BitString.this.bitSet().get(offset, offset + length);
        }

        @Override
        public long[] toLongArray() {
            return bitSet().toLongArray();
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            return BitString.this.get(offset + index);
        }

        @Override
        public int size() {
            return length;
        }

        public BitString and(final BitString other) {
            return standardAnd(other);
        }

        public BitString or(final BitString other) {
            return standardOr(other);
        }

        public BitString xor(final BitString other) {
            return standardXor(other);
        }

        public BitString andNot(final BitString other) {
            return standardAndNot(other);
        }

        @Override
        public Iterable<Integer> asIndices() {
            return FluentIterable.from(BitString.this.asIndices())
                    .filter(new Predicate<Integer>() {
                        @Override
                        public boolean apply(final Integer input) {
                            return input >= offset && input < offset + length;
                        }
                    })
                    .transform(new Function<Integer, Integer>() {
                        @Nullable
                        @Override
                        public Integer apply(final Integer input) {
                            return input - offset;
                        }
                    });
        }
    }

    private static class EmptyString extends BitString {
        public static final EmptyString INSTANCE = new EmptyString();

        private EmptyString() {
        }

        @Override
        public int cardinality() {
            return 0;
        }

        @Override
        protected BitSet bitSet() {
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

        public final BitString and(final BitString other) {
            return zeros(other.size());
        }

        public final BitString or(final BitString other) {
            return other;
        }

        public final BitString xor(final BitString other) {
            return other;
        }

        public final BitString andNot(final BitString other) {
            return zeros(other.size());
        }

        @Override
        public Iterable<Integer> asIndices() {
            return ImmutableSet.of();
        }
    }

    @VisibleForTesting
    static final class CombinedString extends BitString {
        private final BitString bitString1;
        private final BitString bitString2;

        public CombinedString(final BitString bitString1, final BitString bitString2) {
            this.bitString1 = bitString1;
            this.bitString2 = bitString2;
        }

        @Override
        public int cardinality() {
            return bitString1.cardinality() + bitString2.cardinality();
        }

        @Override
        protected BitSet bitSet() {
            final BitSet combinedBitSet = (BitSet) bitString1.bitSet().clone();
            BitSets.set(combinedBitSet, bitString1.size(), bitString2);
            return combinedBitSet;
        }

        @Override
        public long[] toLongArray() {
            return bitSet().toLongArray();
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            if (index < bitString1.size()) {
                return bitString1.get(index);
            } else {
                return bitString2.get((index - bitString1.size()));
            }
        }

        @Override
        public int size() {
            return bitString1.size() + bitString2.size();
        }

        public BitString and(final BitString other) {
            return standardAnd(other);
        }

        public BitString or(final BitString other) {
            return standardOr(other);
        }

        public BitString xor(final BitString other) {
            return standardXor(other);
        }

        public BitString andNot(final BitString other) {
            return standardAndNot(other);
        }

        @Override
        public Iterable<Integer> asIndices() {
            return Iterables.concat(bitString1.asIndices(), FluentIterable.from(bitString2.asIndices())
                    .transform(new Function<Integer, Integer>() {
                        @Nullable
                        @Override
                        public Integer apply(final Integer input) {
                            return input + bitString1.size();
                        }
                    }));
        }
    }

    @VisibleForTesting
    static final class IndexSetString extends BitString {

        private final Set<Integer> indices;
        private final int length;

        IndexSetString(final Set<Integer> indices, final int length) {
            checkArgument(length >= 0);
            checkNotNull(indices);
            checkArgument(Ordering.natural().isOrdered(indices));
            checkArgument(Ordering.natural().compare(Iterables.getFirst(indices, 0), 0) >= 0);
            checkArgument(Ordering.natural().compare(Iterables.getLast(indices, 0), length) < 0);
            this.indices = indices;
            this.length = length;
        }

        @Override
        public int cardinality() {
            return indices.size();
        }

        @Override
        protected BitSet bitSet() {
            final BitSet bitSet = new BitSet(length);
            for (Integer index : indices) {
                bitSet.set(index);
            }
            return bitSet;
        }

        @Override
        public long[] toLongArray() {
            final long[] longs = bitSet().toLongArray();
            return Longs.concat(new long[(size() + 63) / 64 - longs.length], longs);
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            return indices.contains(index);
        }

        @Override
        public int size() {
            return length;
        }

        public BitString and(final BitString other) {
            if (other instanceof IndexSetString) {
                return forIndices(
                        Sets.intersection(indices, ((IndexSetString) other).indices),
                        Math.max(size(), other.size()));
            }
            return standardAnd(other);
        }

        public BitString or(final BitString other) {
            if (other instanceof IndexSetString) {
                return forIndices(
                        Sets.union(indices, ((IndexSetString) other).indices),
                        Math.max(size(), other.size()));
            }
            return standardOr(other);
        }

        public BitString xor(final BitString other) {
            if (other instanceof IndexSetString) {
                return forIndices(
                        Sets.symmetricDifference(indices, ((IndexSetString) other).indices),
                        Math.max(size(), other.size()));
            }
            return standardXor(other);
        }

        public BitString andNot(final BitString other) {
            return standardAndNot(other);
        }

        @Override
        public Iterable<Integer> asIndices() {
            return indices;
        }
    }

    static class InverseString extends BitString {
        private final BitString delegate;

        public InverseString(final BitString delegate) {
            this.delegate = delegate;
        }

        @Override
        public int cardinality() {
            return delegate.size() - delegate.cardinality();
        }

        @Override
        protected BitSet bitSet() {
            return BitSet.valueOf(toLongArray());
        }

        @Override
        public long[] toLongArray() {
            final long[] longs = delegate.toLongArray();
            for (int i = 0; i < longs.length; i++) {
                longs[i] = ~longs[i];
                if (i == longs.length - 1) {
                    longs[i] = longs[i] & (~0L >>> (longs.length * 64 - size()));
                }
            }
            return longs;
        }

        @Override
        public BitString and(final BitString other) {
            return standardAnd(other);
        }

        @Override
        public BitString or(final BitString other) {
            return standardOr(other);
        }

        @Override
        public BitString xor(final BitString other) {
            return standardXor(other);
        }

        @Override
        public BitString andNot(final BitString other) {
            return standardAndNot(other);
        }

        @Override
        public Iterable<Integer> asIndices() {
            final Iterable<Integer> integers = delegate.asIndices();
            final ContiguousSet<Integer> all =
                    ContiguousSet.create(Range.closedOpen(0, size()), DiscreteDomain.integers());

            if (integers instanceof Set) {
                return Sets.difference(all, (Set<Integer>) integers);
            } else {
                return new FluentIterable<Integer>() {
                    @Override
                    public Iterator<Integer> iterator() {
                        return new AbstractIterator<Integer>() {
                            private final Iterator<Integer> allBits = all.iterator();
                            private final Iterator<Integer> setBits = integers.iterator();
                            private Optional<Integer> nextSetBit = Optional.absent();

                            @Override
                            protected Integer computeNext() {
                                if (!nextSetBit.isPresent()) {
                                    setNextSetBit();
                                }

                                while (allBits.hasNext()) {
                                    final Integer next = allBits.next();
                                    if (nextSetBit.get() == -1 || nextSetBit.get() > next) {
                                        return next;
                                    } else {
                                        setNextSetBit();
                                    }
                                }

                                return endOfData();
                            }

                            private void setNextSetBit() {
                                if (setBits.hasNext()) {
                                    nextSetBit = Optional.of(setBits.next());
                                } else {
                                    nextSetBit = Optional.of(-1);
                                }
                            }
                        };
                    }
                };
            }
        }

        @Override
        public Boolean get(final int index) {
            return !delegate.get(index);
        }

        @Override
        public int size() {
            return delegate.size();
        }
    }

    private abstract static class RandomAccessBitString extends BitString {
        @Override
        public final Iterable<Integer> asIndices() {
            return new FluentIterable<Integer>() {
                @Override
                public Iterator<Integer> iterator() {
                    return new AbstractIterator<Integer>() {
                        private int searchIndex = 0;

                        @Override
                        protected Integer computeNext() {
                            final int nextSetIndex = nextSetBit(searchIndex);
                            if (nextSetIndex == -1) {
                                return endOfData();
                            }
                            searchIndex = nextSetIndex + 1;
                            return nextSetIndex;
                        }
                    };
                }
            };
        }

        protected abstract int nextSetBit(final int from);
    }
}
