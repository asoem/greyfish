package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.asoem.greyfish.utils.base.Callbacks.emptyCallback;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 18:01
 */
public class ResourceConsumptionActionTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceConsumptionAction action = ResourceConsumptionAction.with()
                .interactionRadius(constant(0.42))
                .ontology("foo")
                .requestAmount(constant(0.42))
                .uptakeUtilization(emptyCallback())
                .build();

        // when
        final ResourceConsumptionAction copy = Persisters.createCopy(action, JavaPersister.INSTANCE);

        // then
        assertThat(copy).isEqualsToByComparingFields(action);
    }
}
