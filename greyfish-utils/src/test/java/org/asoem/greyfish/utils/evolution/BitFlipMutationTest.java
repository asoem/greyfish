package org.asoem.greyfish.utils.evolution;

import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSequence;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class BitFlipMutationTest {
    @Test
    public void testMutate() throws Exception {
        // given
        final RandomGenerator rng = mock(RandomGenerator.class);
        given(rng.nextLong()).willReturn(4L);
        final double p = 0.5;
        final Mutations.BitFlipMutation mutation = new Mutations.BitFlipMutation(rng, p);

        // when
        final BitSequence mutated = mutation.mutate(BitSequence.zeros(4));

        // then
        verify(rng, only()).nextLong();
        assertThat(mutated, is(equalTo(BitSequence.parse("0100"))));
    }
}
