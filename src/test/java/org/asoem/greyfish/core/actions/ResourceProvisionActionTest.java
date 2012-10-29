package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 18:04
 */
public class ResourceProvisionActionTest {
    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceProvisionAction action = ResourceProvisionAction.with()
                .ontology("foo")
                .provides(Callbacks.constant(42.0))
                .build();

        // when
        final ResourceProvisionAction copy = Persisters.createCopy(action, JavaPersister.INSTANCE);

        // then
        assertThat(copy).isEqualsToByComparingFields(action);
    }
}
