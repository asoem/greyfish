package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class ResourceProvisionActionTest {
    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceProvisionAction<Basic2DAgent> action = ResourceProvisionAction.<Basic2DAgent>with()
                .name("test")
                .ontology("foo")
                .provides(Callbacks.constant(42.0))
                .build();

        // when
        final ResourceProvisionAction<Basic2DAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
