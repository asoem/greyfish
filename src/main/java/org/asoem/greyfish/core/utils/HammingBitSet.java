package org.asoem.greyfish.core.utils;

import java.util.BitSet;

/**
 * User: christoph
 * Date: 01.03.11
 * Time: 16:43
 */
public class HammingBitSet extends BitSet implements Comparable<BitSet> {
    @Override
    public int compareTo(BitSet o) {
        final int lengthDiff = Math.abs(this.length() - o.length());
        final int compareLength = Math.min(this.length(), o.length());

        final BitSet compareBitSet = this.get(0, compareLength);
        compareBitSet.xor(o.get(0, compareLength));

        return lengthDiff + compareBitSet.cardinality();
    }
}
