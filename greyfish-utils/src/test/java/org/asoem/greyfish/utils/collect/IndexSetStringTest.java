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

import com.google.common.collect.Sets;

import java.util.HashSet;

public class IndexSetStringTest extends AbstractBitStringImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        final HashSet<Integer> indices = Sets.newHashSet();
        for (int i = bitString.length() - 1, j = 0; i >= 0; i--, j++) {

            if (bitString.charAt(i) == '1') {
                indices.add(j);
            }
        }
        return new BitString.IndexSetString(indices, bitString.length());
    }
}
