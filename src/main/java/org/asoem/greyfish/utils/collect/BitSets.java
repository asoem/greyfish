package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class BitSets {

    private BitSets() {}

    public static BitSet copyOf(BigInteger bigInteger) {
        checkArgument(bigInteger.signum() >= 0);
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
        return new BigInteger(toByteArray(checkNotNull(bitSet)));
    }

    public static String toString(BitSet bitSet) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i<bitSet.length(); i++)
            builder.append(bitSet.get(i) ? '1' : '0');
        return builder.reverse().toString();
    }

    public static BitSet parse(String s) {
        final List<Boolean> booleans = Lists.transform(Lists.charactersOf(new StringBuffer(s).reverse()), new Function<Character, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(Character character) {
                switch (character) {
                    case '0':
                        return false;
                    case '1':
                        return true;
                    default:
                        throw new IllegalArgumentException("Invalid character: " + character);
                }
            }
        });

        BitSet bitSet = new BitSet(booleans.size());
        for (int i = 0; i < booleans.size(); i++) {
             bitSet.set(i, booleans.get(i));
        }
        return bitSet;
    }
}
