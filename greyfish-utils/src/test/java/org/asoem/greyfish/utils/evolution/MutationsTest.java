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
}
