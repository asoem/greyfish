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

import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class BitSetsTest {
    @Test
    public void testParse() throws Exception {
        // given
        final String s = "001011001101";
        final BitSet bs = new BitSet(s.length());
        bs.set(0, true);
        bs.set(2, true);
        bs.set(3, true);
        bs.set(6, true);
        bs.set(7, true);
        bs.set(9, true);

        // when
        final BitSet parse = BitSets.parse(s);

        // then
        assertThat(parse, is(equalTo(bs)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidString() throws Exception {
        // given
        final String invalid = " 001000100";

        // when
        BitSets.parse(invalid);

        // then
        assertThat("No exception was thrown", false);
    }

    @Test
    public void testParseEmptyString() throws Exception {
        // given
        final String empty = "";

        // when
        final BitSet parse = BitSets.parse(empty);

        // then
        assertThat(parse, is(equalTo(new BitSet(0))));
    }

    @Test
    public void testSwap() throws Exception {
        // given
        final BitSet bitSet1 = BitSets.create(0b010010010L);
        final BitSet bitSet2 = BitSets.create(0b100101100L);

        // when
        BitSets.swap(bitSet1, 5, bitSet2, 2, 2);

        // then
        assertThat(bitSet1, is(equalTo(BitSets.create(0b011110010L))));
        assertThat(bitSet2, is(equalTo(BitSets.create(0b100100000L))));
    }
}
