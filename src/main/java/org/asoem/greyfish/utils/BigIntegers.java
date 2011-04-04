package org.asoem.greyfish.utils;

import com.google.common.base.Preconditions;

import java.math.BigInteger;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 16:32
 */
public class BigIntegers {
    public static int hammingDistance(BigInteger a, BigInteger b) {
        Preconditions.checkArgument(a.bitLength() == b.bitLength());
        return a.xor(b).bitCount();
    }
}
