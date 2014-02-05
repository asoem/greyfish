package org.asoem.greyfish.utils.base;

public final class LongArrays {
    private LongArrays() {}

    public static long bitCount(final long[] longs) {
        long bc = 0;
        for (long aLong : longs) {
            bc += Long.bitCount(aLong);
        }
        return bc;
    }
}
