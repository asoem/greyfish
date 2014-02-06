package org.asoem.greyfish.utils.evolution;

import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSequence;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class NPointCrossoverTest {
    @Test
    public void testRecombine() throws Exception {
        // given
        final BitSequence bitSequence1 = BitSequence.parse("0000");
        final BitSequence bitSequence2 = BitSequence.parse("1111");
        final RandomGenerator rng = mock(RandomGenerator.class);
        given(rng.nextLong()).willReturn(4L);
        Recombinations.NPointCrossover crossover = new Recombinations.NPointCrossover(rng, 0.5);

        // when
        final RecombinationProduct<BitSequence> recombined = crossover.recombine(bitSequence1, bitSequence2);

        // then
        verify(rng, only()).nextLong();
        assertThat(recombined.first(), is(equalTo(BitSequence.parse("1100"))));
        assertThat(recombined.second(), is(equalTo(BitSequence.parse("0011"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecombineDifferentLengths() throws Exception {
        // given
        final BitSequence bitSequence1 = BitSequence.ones(10);
        final BitSequence bitSequence2 = BitSequence.ones(11);
        final RandomGenerator rng = mock(RandomGenerator.class);
        Recombinations.NPointCrossover crossover = new Recombinations.NPointCrossover(rng, 0.5);

        // when
        crossover.recombine(bitSequence1, bitSequence2);

        // then
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void testRecombineNullLeft() throws Exception {
        // given
        final BitSequence bitSequence1 = BitSequence.ones(10);
        final RandomGenerator rng = mock(RandomGenerator.class);
        Recombinations.NPointCrossover crossover = new Recombinations.NPointCrossover(rng, 0.5);

        // when
        crossover.recombine(null, bitSequence1);

        // then
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void testRecombineNullRight() throws Exception {
        // given
        final BitSequence bitSequence1 = BitSequence.ones(10);
        final RandomGenerator rng = mock(RandomGenerator.class);
        Recombinations.NPointCrossover crossover = new Recombinations.NPointCrossover(rng, 0.5);

        // when
        crossover.recombine(bitSequence1, null);

        // then
        fail();
    }
}
