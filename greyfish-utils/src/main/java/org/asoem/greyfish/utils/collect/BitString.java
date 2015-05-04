/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.collect;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.*;
import com.google.common.primitives.Longs;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.math.statistics.Samplings;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

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
     * Computes the <a href="https://en.wikipedia.org/wiki/Hamming_distance">hamming distance</a> between this string
     * and an {@code other} string which must be of equal length.
     *
     * @param other an other string
     * @return the number of positions where the elements in both strings are not equal
     * @throws java.lang.IllegalArgumentException is the lists are not of the same length
     */
    public final int hammingDistance(final BitString other) {
        checkNotNull(other);
        checkArgument(other.size() == size());
        return xor(other).cardinality();
    }

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

    /**
     * Create a random bit string of given {@code length} where each bit is set with probability {@code p}, and not set
     * with probability {@code 1-p}.
     *
     * @param length the length of the bit string
     * @param rng    the random number generator to use
     * @param p      the probability for each bit in the new bit string to hold the value 1
     * @return a new bit string
     */
    public static BitString random(final int length, final RandomGenerator rng, final double p) {
        checkNotNull(rng);
        checkArgument(p >= 0 && p <= 1);
        checkArgument(length >= 0);

        if (length == 0) {
            return emptyBitSequence();
        }

        if (p == 0.5) {
            return random(length, rng); // faster
        }

        final int n;
        if (p == 0) {
            n = 0;
        } else if (p == 1) {
            n = length;
        } else {
            final BinomialDistribution binomialDistribution =
                    new BinomialDistribution(rng, length, p);
            n = binomialDistribution.sample();
        }
        assert n >= 0 && n <= length : n;

        if (n == 0) {
            return zeros(length);
        } else if (n == length) {
            return ones(length);
        }

        final ContiguousSet<Integer> indexRange =
                ContiguousSet.create(Range.closedOpen(0, length), DiscreteDomain.integers());
        final Iterable<Integer> uniqueIndexSample =
                Samplings.random(rng).withoutReplacement().sample(indexRange, n);

        if ((double) n / length < 1.0 / 32) { // < 1 bit per word?
            return new IndexSetString(ImmutableSortedSet.copyOf(uniqueIndexSample), length);
        } else {
            final BitSet bs = new BitSet(length);
            for (Integer index : uniqueIndexSample) {
                bs.set(index, true);
            }
            return new BitSetString(bs, length);
        }
    }

    public static BitString forBitSet(final BitSet bitSet, final int length) {
        return create(bitSet, length);
    }

    public static BitString create(final int length, final long... longs) {
        return create(BitSet.valueOf(longs), length);
    }

    /**
     * Create a new bit string of given {@code length} with bits set to one at given {@code indices}. The indices might
     * be given
     *
     * @param indices the indices of the bits to set
     * @param length  the length of the bit string to create
     * @return a new bit string of given {@code length}
     */
    public static BitString forIndices(final Set<Integer> indices, final int length) {
        if ((double) indices.size() / length < 1.0 / 32) {
            return new IndexSetString(indices, length);
        } else {
            final BitSet bitSet = new BitSet(length);
            for (Integer index : indices) {
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

    /**
     * Find the index of the next set bit starting at index {@code from}.
     *
     * @param from the index to start searching from
     * @return the index of the next set bit, or {@link com.google.common.base.Optional#absent()}.
     */
    public abstract Optional<Integer> nextSetBit(final int from);

    /**
     * Find the index of the previous set bit starting at index {@code from}.
     *
     * @param from the index to start searching from
     * @return the index of the previous set bit, or {@link com.google.common.base.Optional#absent()}.
     */
    public abstract Optional<Integer> previousSetBit(final int from);

    /**
     * Find the index of the next clear bit starting at index {@code from}.
     *
     * @param from the index to start searching from
     * @return the index of the next clear bit, or {@link com.google.common.base.Optional#absent()}.
     */
    public abstract Optional<Integer> nextClearBit(final int from);

    /**
     * Find the index of the previous clear bit starting at index {@code from}.
     *
     * @param from the index to start searching from
     * @return the index of the previous clear bit, or {@link com.google.common.base.Optional#absent()}.
     */
    public abstract Optional<Integer> previousClearBit(final int from);

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
        public Optional<Integer> nextSetBit(final int from) {
            return optionalFromIndex(bitSet.nextSetBit(from));
        }

        private static Optional<Integer> optionalFromIndex(final int index) {
            return index == -1 ? Optional.<Integer>absent() : Optional.of(index);
        }

        @Override
        public Optional<Integer> previousSetBit(final int from) {
            return optionalFromIndex(bitSet.previousSetBit(from));
        }

        @Override
        public Optional<Integer> nextClearBit(final int from) {
            return optionalFromIndex(bitSet.nextClearBit(from));
        }

        @Override
        public Optional<Integer> previousClearBit(final int from) {
            return optionalFromIndex(bitSet.previousClearBit(from));
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

        @Override
        public Optional<Integer> nextSetBit(final int from) {
            checkPositionIndex(from, size());
            final Optional<Integer> nextSetBit = BitString.this.nextSetBit(offset + from);
            if (nextSetBit.isPresent()) {
                return nextSetBit.get() >= offset + length
                        ? Optional.<Integer>absent()
                        : Optional.of(nextSetBit.get() - offset);
            } else {
                return Optional.absent();
            }
        }

        @Override
        public Optional<Integer> previousSetBit(final int from) {
            checkPositionIndex(from, size());
            final Optional<Integer> previousSetBit = BitString.this.previousSetBit(offset + from);
            return previousSetBit.get() < offset
                    ? Optional.<Integer>absent()
                    : Optional.of(previousSetBit.get() - offset);
        }

        @Override
        public Optional<Integer> nextClearBit(final int from) {
            checkPositionIndex(from, size());
            final Optional<Integer> nextClearBit = BitString.this.nextClearBit(offset + from);
            if (nextClearBit.isPresent()) {
                return nextClearBit.get() >= offset + length
                        ? Optional.<Integer>absent()
                        : Optional.of(nextClearBit.get() - offset);
            } else {
                return Optional.absent();
            }
        }

        @Override
        public Optional<Integer> previousClearBit(final int from) {
            checkPositionIndex(from, size());
            final Optional<Integer> previousClearBit = BitString.this.previousClearBit(offset + from);
            return previousClearBit.get() < offset
                    ? Optional.<Integer>absent()
                    : Optional.of(previousClearBit.get() - offset);
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

        @Override
        public Optional<Integer> nextSetBit(final int from) {
            return Optional.absent();
        }

        @Override
        public Optional<Integer> previousSetBit(final int from) {
            return Optional.absent();
        }

        @Override
        public Optional<Integer> nextClearBit(final int from) {
            return Optional.absent();
        }

        @Override
        public Optional<Integer> previousClearBit(final int from) {
            return Optional.absent();
        }
    }

    @VisibleForTesting
    static final class CombinedString extends BitString {
        private final BitString lowString;
        private final BitString highString;

        public CombinedString(final BitString lowString, final BitString highString) {
            this.lowString = lowString;
            this.highString = highString;
        }

        @Override
        public int cardinality() {
            return lowString.cardinality() + highString.cardinality();
        }

        @Override
        protected BitSet bitSet() {
            final BitSet combinedBitSet = (BitSet) lowString.bitSet().clone();
            BitSets.set(combinedBitSet, lowString.size(), highString);
            return combinedBitSet;
        }

        @Override
        public long[] toLongArray() {
            return bitSet().toLongArray();
        }

        @Override
        public Boolean get(final int index) {
            checkElementIndex(index, size());
            if (index < lowString.size()) {
                return lowString.get(index);
            } else {
                return highString.get((index - lowString.size()));
            }
        }

        @Override
        public int size() {
            return lowString.size() + highString.size();
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
            return Iterables.concat(lowString.asIndices(), FluentIterable.from(highString.asIndices())
                    .transform(new Function<Integer, Integer>() {
                        @Nullable
                        @Override
                        public Integer apply(final Integer input) {
                            return input + lowString.size();
                        }
                    }));
        }

        @Override
        public Optional<Integer> nextSetBit(final int from) {
            if (from < lowString.size()) {
                final Optional<Integer> bs1next = lowString.nextSetBit(from);
                if (bs1next.isPresent()) {
                    return bs1next;
                } else {
                    final Optional<Integer> bs2next = highString.nextSetBit(0);
                    return bs2next.isPresent()
                            ? Optional.of(lowString.size() + bs2next.get())
                            : Optional.<Integer>absent();
                }
            } else {
                final Optional<Integer> index = highString.nextSetBit((from - lowString.size()));
                return index.isPresent() && index.get() < size()
                        ? Optional.of(lowString.size() + index.get())
                        : Optional.<Integer>absent();
            }
        }

        @Override
        public Optional<Integer> previousSetBit(final int from) {
            if (from >= lowString.size()) {
                final Optional<Integer> bs2previous = highString.previousSetBit(from - lowString.size());
                if (bs2previous.isPresent()) {
                    return Optional.of(lowString.size() + bs2previous.get());
                } else {
                    return lowString.previousSetBit(lowString.size());
                }
            } else {
                return lowString.previousSetBit(from);
            }
        }

        @Override
        public Optional<Integer> nextClearBit(final int from) {
            if (from < lowString.size()) {
                final Optional<Integer> bs1next = lowString.nextClearBit(from);
                if (bs1next.isPresent()) {
                    return bs1next;
                } else {
                    final Optional<Integer> bs2next = highString.nextClearBit(0);
                    return bs2next.isPresent()
                            ? Optional.of(lowString.size() + bs2next.get())
                            : Optional.<Integer>absent();
                }
            } else {
                final Optional<Integer> index = highString.nextClearBit((from - lowString.size()));
                return index.isPresent() && index.get() < size()
                        ? Optional.of(lowString.size() + index.get())
                        : Optional.<Integer>absent();
            }
        }

        @Override
        public Optional<Integer> previousClearBit(final int from) {
            if (from >= lowString.size()) {
                final Optional<Integer> bs2previous = highString.previousClearBit(from - lowString.size());
                if (bs2previous.isPresent()) {
                    return Optional.of(lowString.size() + bs2previous.get());
                } else {
                    return lowString.previousClearBit(lowString.size());
                }
            } else {
                return lowString.previousClearBit(from);
            }
        }
    }

    @VisibleForTesting
    static final class IndexSetString extends BitString {

        private final SortedSet<Integer> indices;
        private final int length;

        @SuppressWarnings("unchecked")
            // safe cast
        IndexSetString(final Set<Integer> indices, final int length) {
            checkArgument(length >= 0);
            checkNotNull(indices, "indices");

            final ImmutableSortedSet<Integer> sortedIndices = ImmutableSortedSet.copyOf(indices);
            checkArgument(sortedIndices.isEmpty() || sortedIndices.first() >= 0 && sortedIndices.last() < length);

            this.indices = sortedIndices;
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
                        Sets.intersection(indices, ((IndexSetString) other).indices), Math.max(size(), other.size())
                );
            }
            return standardAnd(other);
        }

        public BitString or(final BitString other) {
            if (other instanceof IndexSetString) {
                return forIndices(
                        Sets.union(indices, ((IndexSetString) other).indices), Math.max(size(), other.size())
                );
            }
            return standardOr(other);
        }

        public BitString xor(final BitString other) {
            if (other instanceof IndexSetString) {
                return forIndices(
                        Sets.symmetricDifference(indices, ((IndexSetString) other).indices),
                        Math.max(size(), other.size())
                );
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

        @Override
        public Optional<Integer> nextSetBit(final int from) {
            checkPositionIndex(from, size());
            if (size() == 0) {
                return Optional.absent();
            }

            for (Integer index : indices) {
                if (index >= from) {
                    return Optional.of(index);
                }
            }
            return Optional.absent();
        }

        @Override
        public Optional<Integer> previousSetBit(final int from) {
            checkPositionIndex(from, size());
            if (size() == 0) {
                return Optional.absent();
            }

            final ImmutableSortedSet<Integer> reversed =
                    ImmutableSortedSet.<Integer>reverseOrder().addAll(indices).build();
            for (Integer index : reversed) {
                if (index <= from) {
                    return Optional.of(index);
                }
            }
            return Optional.absent();
        }

        @Override
        public Optional<Integer> nextClearBit(final int from) {
            checkPositionIndex(from, size());
            if (size() == 0) {
                return Optional.absent();
            }

            Optional<Integer> lastSet = Optional.absent();
            for (Integer index : indices) {
                if (index <= from || lastSet.isPresent() && lastSet.get() + 1 == index) {
                    lastSet = Optional.of(index);
                } else {
                    break;
                }
            }

            if (lastSet.isPresent()) {
                if (lastSet.get() < size() - 1) {
                    return Optional.of(lastSet.get() + 1);
                } else {
                    return Optional.absent();
                }
            } else {
                return Optional.of(from);
            }
        }

        @Override
        public Optional<Integer> previousClearBit(final int from) {
            checkPositionIndex(from, size());
            if (size() == 0) {
                return Optional.absent();
            }

            final ImmutableSortedSet<Integer> reversed =
                    ImmutableSortedSet.<Integer>reverseOrder().addAll(indices).build();
            Optional<Integer> previousSet = Optional.absent();
            for (Integer index : reversed) {
                if (index >= from || previousSet.isPresent() && index == previousSet.get() - 1) {
                    previousSet = Optional.of(index);
                } else {
                    break;
                }
            }

            if (previousSet.isPresent()) {
                if (previousSet.get() > 0) {
                    return Optional.of(previousSet.get() - 1);
                } else {
                    return Optional.absent();
                }
            } else {
                return Optional.of(from);
            }
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
                                    if (nextSetBit.isPresent() && nextSetBit.get() <= next) {
                                        setNextSetBit();
                                    } else {
                                        return next;
                                    }
                                }

                                return endOfData();
                            }

                            private void setNextSetBit() {
                                if (setBits.hasNext()) {
                                    nextSetBit = Optional.of(setBits.next());
                                } else {
                                    nextSetBit = Optional.absent();
                                }
                            }
                        };
                    }
                };
            }
        }

        @Override
        public Optional<Integer> nextSetBit(final int from) {
            return delegate.nextClearBit(from);
        }

        @Override
        public Optional<Integer> previousSetBit(final int from) {
            return delegate.previousClearBit(from);
        }

        @Override
        public Optional<Integer> nextClearBit(final int from) {
            return delegate.nextSetBit(from);
        }

        @Override
        public Optional<Integer> previousClearBit(final int from) {
            return delegate.previousSetBit(from);
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
                            final Optional<Integer> nextSetIndex = nextSetBit(searchIndex);
                            if (!nextSetIndex.isPresent()) {
                                return endOfData();
                            }
                            searchIndex = nextSetIndex.get() + 1;
                            return nextSetIndex.get();
                        }
                    };
                }
            };
        }
    }
}
