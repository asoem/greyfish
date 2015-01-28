package org.asoem.greyfish.utils.collect;

import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.base.LongArrays;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public abstract class AbstractBitStringImplementationTest {
    @Test
    public void testToString() throws Exception {
        // given
        final String bitString = "00010001";
        final BitString bitSequence = createSequence(bitString);

        // when
        final String s = bitSequence.toString();

        // then
        assertThat(s, is(equalTo(bitString)));
    }

    protected abstract BitString createSequence(String bitString);


    @Test
    public void testXor() throws Exception {
        // given
        final BitString bitString = createSequence("00000001");

        // when
        final BitString mutated = bitString.xor(createSequence("00000011"));

        // then
        assertThat(mutated.toString(), is(equalTo("00000010")));
    }

    @Test
    public void testAnd() throws Exception {
        // given

        final BitString bs1 = createSequence("100010101");
        final BitString bs2 = createSequence("10000101");

        // when
        final BitString result = bs1.and(bs2);

        // then
        assertThat(result.toString(), is("000000101"));
    }

    @Test
    public void testAndNot() throws Exception {
        // given
        final BitString bs1 = createSequence("100010101");
        final BitString bs2 = createSequence("10000101");

        // when
        final BitString result = bs1.andNot(bs2);

        // then
        assertThat(result.toString(), is(equalTo("100010000")));
    }

    @Test
    public void testZeros() throws Exception {
        // given
        final int sequenceLength = 8;

        // when
        final BitString bitString = BitString.zeros(sequenceLength);

        // then
        assertThat(bitString.toString(), is(equalTo("00000000")));
    }

    @Test
    public void testOnes() throws Exception {
        // given
        final int sequenceLength = 8;

        // when
        final BitString ones = BitString.ones(sequenceLength);

        // then
        assertThat(ones.toString(), is(equalTo("11111111")));
    }

    @Test
    public void testRandom() throws Exception {
        // given
        final RandomGenerator generator = mock(RandomGenerator.class);
        given(generator.nextLong()).willReturn(105L);

        // when
        final BitString bitString = BitString.random(8, generator);

        // then
        verify(generator, only()).nextLong();
        assertThat(bitString.toString(), is(equalTo("01101001")));
    }

    @Test
    public void testConcat() throws Exception {
        // given
        final BitString sequence1 = createSequence("100101010");
        final BitString sequence2 = createSequence("000100111");

        // when
        final BitString concat = BitString.concat(sequence1, sequence2);

        // then
        assertThat(concat.toString(), is(equalTo("000100111100101010")));
    }

    @Test
    public void testSubSequence() throws Exception {
        // given
        final BitString originalSequence = createSequence("001011001000101");

        // when
        final BitString subSequence = originalSequence.subSequence(3, 7);

        // then
        assertThat(subSequence, is(instanceOf(BitString.SubString.class)));
        assertThat(subSequence, is(equalTo(BitString.parse("1000"))));
        assertThat(subSequence.cardinality(), is(1));
        assertThat(subSequence.size(), is(4));
    }

    @Test
    public void testCardinality() throws Exception {
        // given
        final BitString originalSequence = createSequence("001011001000101");

        // when
        final int cardinality = originalSequence.cardinality();

        // then
        assertThat(cardinality, is(6));
    }

    @Test
    public void testEquals() throws Exception {
        // given
        final BitString sequence1 = createSequence("001010010");
        final BitString sequence2 = createSequence("001010010");

        // when
        final boolean equals = sequence1.equals(sequence2);

        // then
        assertThat(equals, is(true));
    }

    @Test
    public void testHashCode() throws Exception {
        // given
        final BitString sequence1 = createSequence("001010010");
        final BitString sequence2 = createSequence("001010010");

        // when
        final int hashCode1 = sequence1.hashCode();
        final int hashCode2 = sequence2.hashCode();

        // then
        assertThat(hashCode1, is(equalTo(hashCode2)));
    }

    @Test
    public void testToLongArray() throws Exception {
        // given
        final BitString sequence1 = createSequence("001010010");

        // when
        final long[] longs = sequence1.toLongArray();

        // then
        assertThat(longs.length, is(equalTo((sequence1.size() + 63) / 64)));
        assertThat(LongArrays.bitCount(longs), is(equalTo((long) sequence1.cardinality())));
    }

    @Test
    public void testGet() throws Exception {
        // given
        final String bitString = "001010010";
        final BitString sequence1 = createSequence(bitString);

        // when
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < sequence1.size(); i++) {
            stringBuilder.append(sequence1.get(i) ? '1' : '0');
        }

        // then
        assertThat(stringBuilder.reverse().toString(), is(equalTo(bitString)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfRange() throws Exception {
        // given
        final String bitString = "001010010";
        final BitString sequence1 = createSequence(bitString);

        // when
        sequence1.get(10);

        // then
        fail();
    }

    @Test
    public void testSetBits() throws Exception {
        // given
        final String bitString = "001010010";
        final BitString sequence1 = createSequence(bitString);

        // when
        final Iterable<Integer> indexes = sequence1.asIndices();

        // then
        assertThat(indexes, contains(1, 4, 6));
    }

    @Test
    public void testNextSetBit() throws Exception {
        // given
        final String bitString = "001010010";
        final BitString sequence1 = createSequence(bitString);

        // when
        final int index = sequence1.nextSetBit(2);

        // then
        assertThat(index, is(equalTo(4)));
    }

    @Test
    public void testPreviousSetBit() throws Exception {
        // given
        final String bitString = "001010010";
        final BitString sequence1 = createSequence(bitString);

        // when
        final int index = sequence1.previousSetBit(7);

        // then
        assertThat(index, is(equalTo(6)));
    }
}
