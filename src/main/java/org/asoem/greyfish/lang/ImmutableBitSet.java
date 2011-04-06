package org.asoem.greyfish.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import org.asoem.greyfish.utils.BitSets;
import org.asoem.greyfish.utils.RandomUtils;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 16:49
 */
public class ImmutableBitSet extends Number implements Comparable<ImmutableBitSet>, Iterable<Boolean> {

    private final BigInteger val;

    public ImmutableBitSet(BigInteger val) {
        this.val = val;
    }

    public ImmutableBitSet(BitSet val) {
        this.val = BitSets.toBigInteger(val);
    }

    public ImmutableBitSet(int length, Random rng) {
        val = new BigInteger(length, rng);
    }

    /**
     * Create a new ImmutableBitSet from {@code val} and mutate each bit with probability {@code p}
     * @param val
     * @param p
     */
    public ImmutableBitSet(ImmutableBitSet val, double p) {
        BitSet bs = new BitSet(val.length());
        int idx = 0;
        for(Boolean b : val)
            bs.set(idx++, b);
        this.val = BitSets.toBigInteger(bs);
    }

    /**
     *
     * @param length The length of this ImmutableBitSet
     * @param p The probability that the bit at a given position is set to true
     */
    public ImmutableBitSet(int length, double p) {
        BitSet bs = new BitSet(length);
        for (int i=0; i<= bs.length(); i++) {
            bs.set(i, RandomUtils.trueWithProbability(p));
        }
        this.val = BitSets.toBigInteger(bs);
    }

    @Override
    public double doubleValue() {
        return val.doubleValue();
    }

    @Override
    public float floatValue() {
        return val.floatValue();
    }

    @Override
    public long longValue() {
        return val.longValue();
    }

    @Override
    public int intValue() {
        return val.intValue();
    }

    public byte[] toByteArray() {
        return val.toByteArray();
    }

    @Override
    public String toString() {
        return val.toString();
    }

    public String toString(int radix) {
        return val.toString(radix);
    }

    @Override
    public int hashCode() {
        return val.hashCode();
    }

    @Override
    public boolean equals(Object x) {
        return val.equals(x);
    }

    @Override
    public int compareTo(ImmutableBitSet val) {
        return this.val.compareTo(val.val);
    }

    public int cardinality() {
        return val.bitCount();
    }

    public int length() {
        return val.bitLength();
    }

    public int getLowestSetBit() {
        return val.getLowestSetBit();
    }

    @Override
    public byte byteValue() {
        return val.byteValue();
    }

    @Override
    public short shortValue() {
        return val.shortValue();
    }

    @Override
    public Iterator<Boolean> iterator() {
        return new AbstractIterator<Boolean>() {
            int idx = 0;
            @Override
            protected Boolean computeNext() {
                if (idx < length())
                    return val.testBit(idx++);
                else
                    return endOfData();
            }
        };
    }

    public ImmutableBitSet subtract(ImmutableBitSet copy) {
        return new ImmutableBitSet(val.subtract(copy.val));
    }

    public static ImmutableBitSet ones(int length) {
        return new ImmutableBitSet(length, 1);
    }

    public static ImmutableBitSet zeros(int length) {
        return new ImmutableBitSet(length, 0);
    }

    public boolean get(int idx) {
       return val.testBit(idx);
    }

    public int hammingDistance(ImmutableBitSet that) {
        Preconditions.checkNotNull(that);

        int modifications = 0;
        for (int i = 0; i<= Math.min(this.length(), that.length()); i++)
            if (this.get(i) ^ that.get(i))
                ++modifications;

        return modifications + Math.abs(this.length() - that.length());
    }

    /**
     * Create a new {@code ImmutableBitSet} from given string {@code s} of '0' and '1' chars
     * @param s a string of '0' and '1' chars
     * @return a new ImmutableBitSet equal to the representation of {@code s}
     */
    public static ImmutableBitSet valueOf(String s) {
        checkArgument(checkNotNull(Pattern.matches("[01]+", s)), "Input must be a sequence of '0' and '1' characters: "+ s);
        BitSet bs = new BitSet(s.length());
        for (int i=0; i< s.length(); ++i)
            switch (s.charAt(i)) {
                case '0' : break;
                case '1' : bs.set(i, true); break;
            }
        return new ImmutableBitSet(bs);
    }
}
