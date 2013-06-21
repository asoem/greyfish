package org.asoem.greyfish.utils.collect;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.apache.commons.math3.random.RandomGenerator;

import java.math.BigInteger;
import java.util.BitSet;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.math.RandomGenerators.nextBoolean;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 16:49
 */
public class BitSequence extends AbstractLinearSequence<Boolean> implements Comparable<BitSequence> {

    private final BitSet bitSet;
    private final int length;

    private BitSequence(BitSet bitSet, int length) {
        assert bitSet != null;

        this.bitSet = bitSet;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String toString() {
        return Strings.padStart(BitSets.toString(bitSet), length, '0');
    }

    @Override
    public Boolean get(int index) {
        checkElementIndex(index, length());
        return bitSet.get(index);
    }

    public int cardinality() {
        return bitSet.cardinality();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BitSequence that = (BitSequence) o;

        if (length != that.length) return false;
        if (!bitSet.equals(that.bitSet)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bitSet.hashCode();
        result = 31 * result + length;
        return result;
    }

    @Override
    public int compareTo(BitSequence val) {
        return this.asBigInteger().compareTo(val.asBigInteger());
    }

    public BigInteger asBigInteger() {
        return new BigInteger(BitSets.toByteArray(bitSet));
    }

    public BitSet asBitSet() {
        return (BitSet) bitSet.clone();
    }

    public BitSequence and(BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.and(this.bitSet);
        return new BitSequence(bitSet, Math.max(length(), bs.length()));
    }

    public BitSequence or(BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.or(this.bitSet);
        return new BitSequence(bitSet, Math.max(length(), bs.length()));
    }

    public BitSequence xor(BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.xor(this.bitSet);
        return new BitSequence(bitSet, Math.max(length(), bs.length()));
    }

    public BitSequence andNot(BitSequence bs) {
        final BitSet bitSet = bs.asBitSet();
        bitSet.andNot(this.bitSet);
        return new BitSequence(bitSet, Math.max(length(), bs.length()));
    }

    private static BitSequence create(int length, BitSet bitSet) {
        return new BitSequence(bitSet, length);
    }

    public static BitSequence ones(int length) {
        return create(length, BitSets.newBitSet(length, true));
    }

    public static BitSequence zeros(int length) {
        return create(length, BitSets.newBitSet(length, false));
    }

    public static BitSequence forIterable(Iterable<? extends Boolean> val) {
        checkNotNull(val);
        final BitSet bs = new BitSet();
        int idx = 0;
        for(boolean b : val)
            bs.set(idx++, b);
        return create(idx, bs);
    }

    public static BitSequence concat(BitSequence sequence1, BitSequence sequence2) {
        checkNotNull(sequence1);
        checkNotNull(sequence2);
        return forIterable(Iterables.concat(sequence1, sequence2));
    }

    /**
     * Create a new {@code BitSequence} from given string {@code s} of '0' and '1' chars
     * @param s a string of '0' and '1' chars
     * @return a new BitSequence equal to the representation of {@code s}
     */
    public static BitSequence parse(String s) {
        return new BitSequence(BitSets.parse(s), s.length());
    }

    public static BitSequence random(int length, RandomGenerator rng) {
        checkNotNull(rng);
        final BitSet bs = new BitSet(length);
        int idx = 0;
        for (int i=0; i<length; ++i)
            bs.set(idx++, rng.nextBoolean());
        return new BitSequence(bs, length);
    }

    public static BitSequence random(int length, RandomGenerator rng, double p) {
        checkNotNull(rng);
        final BitSet bs = new BitSet(length);
        int idx = 0;
        for (int i=0; i<length; ++i)
            bs.set(idx++, nextBoolean(rng, p));
        return new BitSequence(bs, length);
    }
}
