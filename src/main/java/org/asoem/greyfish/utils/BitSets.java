package org.asoem.greyfish.utils;

import java.math.BigInteger;
import java.util.BitSet;

public class BitSets {

    public static BitSet copyOf(BigInteger bigInteger) {
        BitSet ret = new BitSet(bigInteger.bitCount());
        for (int i=0; i <= bigInteger.bitCount(); i++)
            ret.set(i, bigInteger.testBit(i));
        return ret;
    }

    public static byte[] toByteArray(BitSet bitSet) {
        byte[] bytes = new byte[bitSet.length()/8+1];
        for (int i=0; i<bitSet.length(); i++) {
            if (bitSet.get(i)) {
                bytes[bytes.length-i/8-1] |= 1<<(i%8);
            }
        }
        return bytes;
    }

    public static BigInteger toBigInteger(BitSet bitSet) {
        return new BigInteger(toByteArray(bitSet));
    }
}
