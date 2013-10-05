package org.asoem.greyfish.utils.collect;

import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class FunctionalFifoBufferTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final FunctionalFifoBuffer<Integer> messageBox = FunctionalFifoBuffer.withCapacity(1);
        messageBox.add(1);

        // when
        final FunctionalFifoBuffer<Integer> copy = Persisters.copyAsync(messageBox, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(messageBox)));
    }
}
