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

import static org.hamcrest.Matchers.*;


public class Geometry2DTest {
    @Test
    public void testNoIntersectionWithAdjacentFP() throws Exception {

        // when
        final ImmutablePoint2D intersection = Geometry2D.intersection(1.0, 0.0, 1.0, Math.nextAfter(1.0, -Double.MIN_VALUE), 0.0, 1.0, 1.0, 1.0);

        // then
        MatcherAssert.assertThat(intersection, is(nullValue()));
    }

    @Test
    public void testIntersection() throws Exception {

        // when
        final ImmutablePoint2D intersection = Geometry2D.intersection(9.975393084761107, 0.5, 10.066768813786554, 0.5, Math.nextAfter(10.0, -Double.MIN_VALUE), 0.0, Math.nextAfter(10.0, -Double.MIN_VALUE), 1.0);

        // then
        MatcherAssert.assertThat(intersection, is(equalTo(ImmutablePoint2D.at(Math.nextAfter(10.0, -Double.MIN_VALUE), 0.5))));
    }
}
