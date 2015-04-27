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

package org.asoem.greyfish.utils.space;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class ImmutableLocation2DTest {

    @Test
    public void testEqualsNegativeZero() throws Exception {
        final ImmutablePoint2D negZero = ImmutablePoint2D.at(-0.0, -0.0);
        final ImmutablePoint2D posZero = ImmutablePoint2D.at(0.0, 0.0);

        MatcherAssert.assertThat(negZero, is(equalTo(posZero)));
    }
}
