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

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitString;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class UniformCrossoverTest {
    @Test
    public void testRecombine() throws Exception {
        // given
        final BitString bitString1 = BitString.parse("110000");
        final BitString bitString2 = BitString.parse("001111");
        Recombinations.UniformCrossover crossover =
                new Recombinations.UniformCrossover(Functions.constant(ImmutableList.of(2)));

        // when
        final RecombinationProduct<BitString> recombined = crossover.recombine(bitString1, bitString2);

        // then
        assertThat(recombined.first(), is(equalTo(BitString.parse("110100"))));
        assertThat(recombined.second(), is(equalTo(BitString.parse("001011"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecombineDifferentLengths() throws Exception {
        // given
        final BitString bitString1 = BitString.ones(10);
        final BitString bitString2 = BitString.ones(11);
        final RandomGenerator rng = mock(RandomGenerator.class);
        Recombinations.UniformCrossover crossover = new Recombinations.UniformCrossover(rng, 0.5);

        // when
        crossover.recombine(bitString1, bitString2);

        // then
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void testRecombineNullLeft() throws Exception {
        // given
        final BitString bitString1 = BitString.ones(10);
        final RandomGenerator rng = mock(RandomGenerator.class);
        Recombinations.UniformCrossover crossover = new Recombinations.UniformCrossover(rng, 0.5);

        // when
        crossover.recombine(null, bitString1);

        // then
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void testRecombineNullRight() throws Exception {
        // given
        final BitString bitString1 = BitString.ones(10);
        final RandomGenerator rng = mock(RandomGenerator.class);
        Recombinations.UniformCrossover crossover = new Recombinations.UniformCrossover(rng, 0.5);

        // when
        crossover.recombine(bitString1, null);

        // then
        fail();
    }
}
