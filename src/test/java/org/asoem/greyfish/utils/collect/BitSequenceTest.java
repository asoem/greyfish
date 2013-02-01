package org.asoem.greyfish.utils.collect;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
    public void testNewMutatedCopy() throws Exception {
        // given
        final BitSequence bitSequence = BitSequence.parse("00000001");

        // when
        final BitSequence mutated = BitSequence.newMutatedCopy(bitSequence, 1);

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
}
