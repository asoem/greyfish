package org.asoem.greyfish.core.actions;

import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:40
 */
public class MatingReceiverActionTest {
    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public MatingReceiverActionTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }
    
    @Test
    public void testPersistence() throws Exception {
        // given
        final MatingReceiverAction action = MatingReceiverAction.with()
                .ontology("foo")
                .matingProbability(expressionFactory.compile("0.42"))
                .interactionRadius(42.0)
                .spermStorage(new EvaluatedGenomeStorage()).build();

        // when
        final MatingReceiverAction copy = Persisters.createCopy(action, MatingReceiverAction.class, persister);

        // then
        assertThat(copy.getOntology()).isEqualTo("foo");
        assertThat(copy.getMatingProbability().getExpression()).isEqualTo("0.42");
        assertThat(copy.getInteractionRadius()).isEqualTo(42.0);
        assertThat(copy.getSpermStorage()).isNotNull();
    }
}
