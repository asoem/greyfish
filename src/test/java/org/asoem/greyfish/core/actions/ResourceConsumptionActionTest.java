package org.asoem.greyfish.core.actions;

import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

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
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final ResourceConsumptionAction action = ResourceConsumptionAction.with()
                .interactionRadius(expressionFactory.compile("0.42"))
                .ontology("foo")
                .requestAmount(expressionFactory.compile("0.42"))
                .uptakeUtilization(expressionFactory.compile("0.42"))
                .build();

        // when
        final ResourceConsumptionAction copy = Persisters.createCopy(action, ResourceConsumptionAction.class, persister);

        // then
        assertThat(copy.getInteractionRadius().getExpression()).isEqualTo("0.42");
        assertThat(copy.getRequestAmount().getExpression()).isEqualTo("0.42");
        assertThat(copy.getUptakeUtilization().getExpression()).isEqualTo("0.42");
        assertThat(copy.getOntology()).isEqualTo("foo");
    }
}
