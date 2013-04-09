package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.asoem.greyfish.utils.base.Callbacks.emptyCallback;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 18:01
 */
public class ResourceConsumptionActionTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceConsumptionAction<DefaultGreyfishAgent> action = ResourceConsumptionAction.<DefaultGreyfishAgent>with()
                .name("test")
                .interactionRadius(constant(0.42))
                .ontology("foo")
                .requestAmount(constant(0.42))
                .uptakeUtilization(emptyCallback())
                .build();

        // when
        final ResourceConsumptionAction<DefaultGreyfishAgent> copy = Persisters.createCopy(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
