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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.BitSet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class BitSets {

    private BitSets() {
    }

    public static BitSet newBitSet(final int length, final boolean init) {
        final BitSet bs = new BitSet(length);
        if (init) {
            for (int i = 0; i < length; i++) {
                bs.set(i, true);
            }
        }
        return bs;
    }

    public static BitSet copyOf(final BigInteger bigInteger) {
        checkArgument(bigInteger.signum() >= 0);
        final BitSet ret = new BitSet(bigInteger.bitCount());
        for (int i = 0; i <= bigInteger.bitCount(); i++) {
            ret.set(i, bigInteger.testBit(i));
        }
        return ret;
    }

    public static byte[] toByteArray(final BitSet bitSet) {
        final byte[] bytes = new byte[bitSet.length() / 8 + 1];
        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    public static BigInteger toBigInteger(final BitSet bitSet) {
        return new BigInteger(toByteArray(checkNotNull(bitSet)));
    }

    public static String toString(final BitSet bitSet) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) {
            builder.append(bitSet.get(i) ? '1' : '0');
        }
        return builder.reverse().toString();
    }

    /**
     * Parses a string of '0' and '1' characters interpreted in big-endian format and transforms it into a BitSet.
     * <p>Whitespaces are ignored.</p>
     *
     * @param s the input string
     * @return A {@code BitSet} whose bits at indices are set to {@code true}, when the reversed input string has a '1'
     * character at the same index.
     */
    public static BitSet parse(final String s) {
        checkNotNull(s);
        checkArgument(s.matches("^[01]*$"), "Invalid characters (other than '0' or '1') in string: %s", s);

        final ImmutableList<Character> characters = Lists.charactersOf(s);
        final BitSet bitSet = new BitSet(characters.size());
        for (int i = 0, j = characters.size() - 1; i < characters.size() && j >= 0; i++, j--) {
            final Character character = characters.get(j);
            switch (character) {
                case '1':
                    bitSet.set(i);
                    break;
                case '0':
                    break;
                default:
                    if (!Character.isWhitespace(character)) {
                        throw new IllegalArgumentException("Invalid character at position " + j);
                    }
            }
        }

        return bitSet;
    }

    public static void set(final BitSet bitSet, final int offset, final Iterable<Boolean> booleans) {
        checkNotNull(bitSet);
        checkNotNull(booleans);

        int i = offset;
        for (Boolean aBoolean : booleans) {
            bitSet.set(i++, aBoolean);
        }
    }

    public static BitSet create(final BitString bitString) {
        checkNotNull(bitString);

        return (BitSet) bitString.bitSet().clone();
    }

    public static void swap(final BitSet bitSet1, final int offset1,
                            final BitSet bitSet2, final int offset2,
                            final int length) {
        checkNotNull(bitSet1);
        checkNotNull(bitSet2);

        for (int i = 0; i < length; i++) {
            final int pos1 = offset1 + i;
            final boolean b1 = bitSet1.get(pos1);
            final int pos2 = offset2 + i;
            final boolean b2 = bitSet2.get(pos2);
            bitSet1.set(pos1, b2);
            bitSet2.set(pos2, b1);
        }
    }

    public static BitSet create(final long... longs) {
        return BitSet.valueOf(longs);
    }
}
