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

package org.asoem.greyfish.utils.evolution;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitString;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NPointRandomTest {

    @Test
    public void testCreateMutationMap() throws Exception {
        // given
        final int n = 2;
        final RandomGenerator rng = RandomGenerators.rng(0);
        final Mutations.NPointRandom nPointRandom = new Mutations.NPointRandom(rng, n);

        // when
        final ImmutableMap<Integer, Boolean> mutationMap = nPointRandom.createMutationMap(10);

        // then
        assertThat(mutationMap.size(), is(2));
    }

    @Test
    public void testMutate() throws Exception {
        // given
        final BitString input = BitString.parse("1000");
        final ImmutableMap<Integer, Boolean> mutationMap = ImmutableMap.of(0, true, 3, false);

        // when
        final BitString mutation = Mutations.NPointRandom.mutate(input, mutationMap);

        // then
        assertThat(mutation, is(equalTo(BitString.parse("0001"))));
    }
}