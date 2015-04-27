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

package org.asoem.greyfish.impl.space;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ASKDTreeTest {

    @Test
    public void testConstruction() throws Exception {
        // given
        final ImmutableMap<Object, Point2D> map = ImmutableMap.<Object, Point2D>of(
                new Object(), ImmutablePoint2D.at(0.0, 1.0),
                new Object(), ImmutablePoint2D.at(0.3, 2.0));
        final int dimensions = 2;

        // when
        final ASKDTree<Object> tree =
                new ASKDTree<>(dimensions, map);

        // then
        assertThat(tree.size(), is(equalTo(2)));
    }
}