package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class GenericActionTest {
    @Test
    public void testPersistence() throws Exception {
        // given
        final GenericAction<Basic2DAgent> action = GenericAction.<Basic2DAgent>builder()
                .name("test")
                .executes(Callbacks.emptyCallback()).build();

        // when
        final GenericAction<Basic2DAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
