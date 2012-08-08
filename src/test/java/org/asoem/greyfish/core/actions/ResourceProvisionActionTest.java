package org.asoem.greyfish.core.actions;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 18:04
 */
public class ResourceProvisionActionTest {
    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public ResourceProvisionActionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceProvisionAction action = ResourceProvisionAction.with()
                .ontology("foo")
                .provides(Callbacks.constant(42.0))
                .build();

        // when
        final ResourceProvisionAction copy = Persisters.createCopy(action, ResourceProvisionAction.class, persister);

        // then
        assertThat(copy.getOntology()).isEqualTo("foo");
    }
}
