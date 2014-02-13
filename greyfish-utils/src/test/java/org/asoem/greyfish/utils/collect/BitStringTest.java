package org.asoem.greyfish.utils.collect;

import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BitStringTest {
    @Test
    public void testRandom() throws Exception {
        // given
        final int length = 10;
        final RandomGenerator rng = mock(RandomGenerator.class);
        given(rng.nextLong()).willReturn(~0L);

        // when
        final BitString bitString = BitString.random(length, rng);

        // then
        assertThat(bitString, is(equalTo(BitString.ones(length))));
    }

    @Test
    public void testRandomSmallP() throws Exception {
        // given
        final int length = 1000;

        // when
        final BitString bitString = BitString.random(length, RandomGenerators.rng(), 1.0 / length);

        // then
        assertThat(bitString, is(instanceOf(BitString.IndexSetString.class)));
    }
}
