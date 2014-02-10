package org.asoem.greyfish.utils.evolution;

import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitString;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class UniformCrossoverTest {
    @Test
    public void testRecombine() throws Exception {
        // given
        final BitString bitString1 = BitString.parse("110000");
        final BitString bitString2 = BitString.parse("001111");
        final RandomGenerator rng = mock(RandomGenerator.class);
        given(rng.nextLong()).willReturn(4L);
        Recombinations.UniformCrossover crossover = new Recombinations.UniformCrossover(rng, 0.5);

        // when
        final RecombinationProduct<BitString> recombined = crossover.recombine(bitString1, bitString2);

        // then
        verify(rng, only()).nextLong();
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
