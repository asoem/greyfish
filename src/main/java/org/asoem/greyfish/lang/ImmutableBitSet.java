package org.asoem.greyfish.lang;

import com.google.common.collect.AbstractIterator;
import org.asoem.greyfish.utils.BitSets;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;

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
        BitSet bs = new BitSet(val.bitLength());
        int idx = 0;
        for(Boolean b : val)
            bs.set(idx++, b);
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

    public int bitCount() {
        return val.bitCount();
    }

    public int bitLength() {
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
                if (idx < bitLength())
                    return val.testBit(idx++);
                else
                    return endOfData();
            }
        };
    }

    public ImmutableBitSet subtract(ImmutableBitSet copy) {
        return new ImmutableBitSet(val.subtract(copy.val));
    }
}
