package org.asoem.greyfish.utils.collect;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OnesTest {
    @Test
    public void testCardinality() throws Exception {
        // given
        final int length = 10;
        final BitString.Ones zeros = new BitString.Ones(length);

        // when
        final int cardinality = zeros.cardinality();

        // then
        assertThat(cardinality, is(length));
    }
}
