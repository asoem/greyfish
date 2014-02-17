package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.asoem.greyfish.utils.base.Callbacks.emptyCallback;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class ResourceConsumptionActionTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceConsumptionAction<Basic2DAgent> action = ResourceConsumptionAction.<Basic2DAgent>with()
                .name("test")
                .interactionRadius(constant(0.42))
                .ontology("foo")
                .requestAmount(constant(0.42))
                .uptakeUtilization(emptyCallback())
                .build();

        // when
        final ResourceConsumptionAction<Basic2DAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
