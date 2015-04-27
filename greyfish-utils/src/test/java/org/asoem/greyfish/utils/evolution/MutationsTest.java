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

import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitString;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class MutationsTest {
    @Test
    public void testBitFlipMutation0() throws Exception {
        // given
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        final Mutation<BitString> bitStringMutation = Mutations.bitFlipMutation(rng, 0);

        // then
        assertThat(bitStringMutation, is(notNullValue()));
    }

    @Test
    public void testBitFlipMutation1() throws Exception {
        // given
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        final Mutation<BitString> bitStringMutation = Mutations.bitFlipMutation(rng, 1);

        // then
        assertThat(bitStringMutation, is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitFlipMutationNegative() throws Exception {
        // given
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        Mutations.bitFlipMutation(rng, -1);

        // then
        fail();
    }

    @Test
    public void testNPointRandomPositiveN() throws Exception {
        // given
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        final Mutation<BitString> bitStringMutation = Mutations.nPointRandom(rng, 0);

        // then
        assertThat(bitStringMutation, is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNPointRandomNegativeN() throws Exception {
        // given
        final RandomGenerator rng = mock(RandomGenerator.class);

        // when
        Mutations.nPointRandom(rng, -1);

        // then
        fail();
    }
}
