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

public class CombinedStringTest extends AbstractBitStringImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        final BitString lowString = BitString.parse(bitString.substring(bitString.length() / 2, bitString.length()));
        final BitString highString = BitString.parse(bitString.substring(0, bitString.length() / 2));
        return new BitString.CombinedString(lowString, highString);
    }
}
