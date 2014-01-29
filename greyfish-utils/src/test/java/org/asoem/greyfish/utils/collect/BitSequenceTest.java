package org.asoem.greyfish.utils.collect;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public abstract class BitSequenceTest {
    @Test
    public void testToString() throws Exception {
        // given
        final String bitString = "00010001";
        final BitSequence bitSequence = createSequence(bitString);

        // when
        final String s = bitSequence.toString();

        // then
        assertThat(s, is(equalTo(bitString)));
    }

    protected abstract BitSequence createSequence(String bitString);

    @Test
    public void testXor() throws Exception {
        // given
        final BitSequence bitSequence = createSequence("00000001");

        // when
        final BitSequence mutated = bitSequence.xor(BitSequence.ones(bitSequence.length()));

        // then
        assertThat(mutated.toString(), is(equalTo("11111110")));
    }

    @Test
    public void testAnd() throws Exception {
        // given

        final BitSequence bs1 = createSequence("100010101");
        final BitSequence bs2 = createSequence("10000101");

        // when
        final BitSequence result = bs1.and(bs2);

        // then
        assertThat(result.toString(), is("000000101"));
    }

    @Test
    public void testAndNot() throws Exception {
        // given
        final BitSequence bs1 = createSequence("100010101");
        final BitSequence bs2 = createSequence("10000101");

        // when
        final BitSequence result = bs1.andNot(bs2);

        // then
        assertThat(result.toString(), is(equalTo("100010000")));
    }

    @Test
    public void testZeros() throws Exception {
        // given
        final int sequenceLength = 8;

        // when
        final BitSequence bitSequence = BitSequence.zeros(sequenceLength);

        // then
        assertThat(bitSequence.toString(), is(equalTo("00000000")));
    }

    @Test
    public void testOnes() throws Exception {
        // given
        final int sequenceLength = 8;

        // when
        final BitSequence ones = BitSequence.ones(sequenceLength);

        // then
        assertThat(ones.toString(), is(equalTo("11111111")));
    }

    @Test
    public void testRandom() throws Exception {
        // given
        final RandomGenerator generator = mock(RandomGenerator.class);
        given(generator.nextBoolean()).willReturn(true, false, false, true, false, true, true, false);

        // when
        final BitSequence bitSequence = BitSequence.random(8, generator);

        // then
        assertThat(bitSequence.toString(), is(equalTo("01101001")));
    }

    @Test
    public void testConcat() throws Exception {
        // given
        final BitSequence sequence1 = createSequence("100101010");
        final BitSequence sequence2 = createSequence("000100111");

        // when
        final BitSequence concat = BitSequence.concat(sequence1, sequence2);

        // then
        assertThat(concat.toString(), is(equalTo("100101010000100111")));
    }

    @Test
    public void testSubSequence() throws Exception {
        // given
        final BitSequence originalSequence = createSequence("001011001000101");

        // when
        final BitSequence subSequence = originalSequence.subSequence(3, 7);

        // then
        assertThat(subSequence, is(instanceOf(BitSequence.BitSequenceView.class)));
        assertThat(subSequence, is(equalTo(BitSequence.parse("1000"))));
        assertThat(subSequence.cardinality(), is(1));
        assertThat(subSequence.length(), is(4));
    }

    @Test
    public void testCardinality() throws Exception {
        // given
        final BitSequence originalSequence = createSequence("001011001000101");

        // when
        final int cardinality = originalSequence.cardinality();

        // then
        assertThat(cardinality, is(6));
    }
}
