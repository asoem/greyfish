package org.asoem.greyfish.utils.collect;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 01.02.13
 * Time: 10:16
 */
public class BitSequenceTest {
    @Test
    public void testToString() throws Exception {
        // given
        final BitSequence bitSequence = BitSequence.parse("00010001");

        // when
        String s = bitSequence.toString();

        // then
        assertThat(s, is(equalTo("00010001")));
    }

    @Test
    public void testXor() throws Exception {
        // given
        final BitSequence bitSequence = BitSequence.parse("00000001");

        // when
        final BitSequence mutated = bitSequence.xor(BitSequence.ones(bitSequence.length()));

        // then
        assertThat(mutated.toString(), is(equalTo("11111110")));
    }

    @Test
    public void testAnd() throws Exception {
        // given
        final BitSequence bs1 = BitSequence.parse("100010101");
        final BitSequence bs2 = BitSequence.parse("10000101");

        // when
        final BitSequence result = bs1.and(bs2);

        // then
        assertThat(result, is(equalTo(BitSequence.parse("000000101"))));
    }

    @Test
    public void testZeros() throws Exception {
        // given
        final BitSequence bitSequence = BitSequence.zeros(8);

        // when
        final String asString = bitSequence.toString();

        // then
        assertThat(asString, is(equalTo("00000000")));
    }

    @Test
    public void testOnes() throws Exception {
        // given
        final BitSequence bitSequence = BitSequence.ones(8);

        // when
        final String asString = bitSequence.toString();

        // then
        assertThat(asString, is(equalTo("11111111")));
    }

    @Test
    public void testRandom() throws Exception {
        // given
        final RandomGenerator generator = mock(RandomGenerator.class);
        given(generator.nextBoolean()).willReturn(true, false, false, true, false, true, true, false);
        final BitSequence bitSequence = BitSequence.random(8, generator);

        // when
        final String asString = bitSequence.toString();

        // then
        assertThat(asString, is(equalTo("01101001")));
    }
}
