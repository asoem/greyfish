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

package org.asoem.greyfish.utils.base;

import java.util.Arrays;

/**
 * Utility methods for arrays of type {@code long[]} that are not already found in {@link
 * com.google.common.primitives.Longs}, {@link Long} or {@link Arrays}.
 */
public final class LongArrays {
    private LongArrays() {}

    /**
     * Calculate the sum of the bit count for each long in {@code array}.
     *
     * @param array the longs to count the bits for
     * @return the sum of the bit count for each long
     */
    public static long bitCount(final long... array) {
        long bc = 0;
        for (long aLong : array) {
            bc += Long.bitCount(aLong);
        }
        return bc;
    }
}
