package org.asoem.greyfish.utils.evolution;

import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSequence;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
}
