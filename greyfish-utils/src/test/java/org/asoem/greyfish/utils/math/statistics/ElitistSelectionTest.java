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

package org.asoem.greyfish.utils.math.statistics;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class ElitistSelectionTest {
    @Test
    public void testSample() throws Exception {
        // given
        final Sampling<Comparable<?>> sampling = new Samplings.ElitistSelection<>(Ordering.natural());
        final FunctionalList<Integer> integers = ImmutableFunctionalList.of(1, 4, 5, 0, 3);

        // when
        final Iterable<Integer> sample = sampling.sample(integers, 3);

        // then
        assertThat(sample, Matchers.containsInAnyOrder(5, 4, 3));
    }
}
