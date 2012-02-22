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
 * Time: 17:49
 */
public class MatingTransmitterActionTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public MatingTransmitterActionTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }
    
    @Test
    public void testPersistence() throws Exception {
        // given
        final MatingTransmitterAction action = MatingTransmitterAction.with()
                .ontology("foo")
                .matingProbability(expressionFactory.compile("0.42"))
                .spermFitness(expressionFactory.compile("0.42"))
                .build();

        // when
        final MatingTransmitterAction copy = Persisters.createCopy(action, MatingTransmitterAction.class, persister);

        // then
        assertThat(copy.getOntology()).isEqualTo("foo");
        assertThat(copy.getMatingProbability().getExpression()).isEqualTo("0.42");
        assertThat(copy.getSpermFitness().getExpression()).isEqualTo("0.42");
    }
}
