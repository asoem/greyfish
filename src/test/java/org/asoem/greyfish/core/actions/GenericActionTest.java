package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 18:08
 */
public class GenericActionTest {
    @Test
    public void testPersistence() throws Exception {
        // given
        final GenericAction<DefaultGreyfishAgent> action = GenericAction.<DefaultGreyfishAgent>builder()
                .executes(Callbacks.emptyCallback()).build();

        // when
        final GenericAction<DefaultGreyfishAgent> copy = Persisters.createCopy(action, JavaPersister.INSTANCE);

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
