package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.math3.random.RandomGenerator;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.math.RandomUtils.nextBoolean;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 16:49
 */
public class BitSequence extends AbstractLinearSequence<Boolean> implements Comparable<BitSequence> {

    private final BitSet bitSet;
    private final int length;

    private BitSequence(BitSet bitSet, int length) {
        this.length = length;
        this.bitSet = bitSet;
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

    /**
     *
     * @param bitSequence the original BitSequence
     * @param p the probability with which each bit will be mutated
     * @return a new BitSequence with each bit mutated with probability {@code p}
     */
    public static BitSequence mutate(BitSequence bitSequence, final double p) {
        final List<Boolean> booleans = Lists.transform(bitSequence, new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean b) {
                return nextBoolean(p) ? !b : b;
            }
        });
        return forIterable(booleans);
    }

    public static BitSequence ones(int length) {
        return fill(length, true);
    }

    public static BitSequence zeros(int length) {
        return fill(length, false);
    }

    private static BitSequence fill(int length, boolean b) {
        return forIterable(Iterables.limit(Iterables.cycle(b), length));
    }

    public static BitSequence forIterable(Iterable<Boolean> val) {
        checkNotNull(val);
        BitSet bs = new BitSet();
        int idx = 0;
        for(boolean b : val)
            bs.set(idx++, b);
        return new BitSequence(bs, idx);
    }

    /**
     * Create a new {@code BitSequence} from given string {@code s} of '0' and '1' chars
     * @param s a string of '0' and '1' chars
     * @return a new BitSequence equal to the representation of {@code s}
     */
    public static BitSequence parse(String s) {
        return new BitSequence(BitSets.parse(s), s.length());
    }

    public int cardinality() {
        return bitSet.cardinality();
    }

    public static BitSequence random(int length, RandomGenerator generator) {
        checkNotNull(generator);
        BitSet bs = new BitSet();
        int idx = 0;
        for (int i=0; i<length; ++i)
            bs.set(idx++, generator.nextBoolean());
        return new BitSequence(bs, length);
    }
}
