package org.asoem.greyfish.utils.collect;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ZerosTest {
    @Test
    public void testCardinality() throws Exception {
        // given
        final BitString.Zeros zeros = new BitString.Zeros(10);

        // when
        final int cardinality = zeros.cardinality();

        // then
        assertThat(cardinality, is(0));
    }
}