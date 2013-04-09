package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 18:04
 */
public class ResourceProvisionActionTest {
    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceProvisionAction<DefaultGreyfishAgent> action = ResourceProvisionAction.<DefaultGreyfishAgent>with()
                .name("test")
                .ontology("foo")
                .provides(Callbacks.constant(42.0))
                .build();

        // when
        final ResourceProvisionAction<DefaultGreyfishAgent> copy = Persisters.createCopy(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
