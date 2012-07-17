package org.asoem.greyfish.core.actions;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.asoem.greyfish.utils.base.Callbacks.emptyCallback;
import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 18:01
 */
public class ResourceConsumptionActionTest {
    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public ResourceConsumptionActionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

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
        final ResourceConsumptionAction copy = Persisters.createCopy(action, ResourceConsumptionAction.class, persister);

        // then
        assertThat(copy.getInteractionRadius()).isEqualTo(constant(0.42));
        assertThat(copy.getRequestAmount()).isEqualTo(constant(0.42));
        assertThat(copy.getUptakeUtilization()).isEqualTo(emptyCallback());
        assertThat(copy.getOntology()).isEqualTo("foo");
    }
}
