package org.asoem.greyfish.utils.collect;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BitStringTest {
    @Test
    public void testRandom() throws Exception {
        // given
        final int length = 10;
        final RandomGenerator rng = mock(RandomGenerator.class);
        given(rng.nextLong()).willReturn(0xffffffffffffffffL);

        // when
        final BitString bitString = BitString.random(length, rng);

        // then
        assertThat(bitString, is(equalTo(BitString.ones(length))));
    }
}
