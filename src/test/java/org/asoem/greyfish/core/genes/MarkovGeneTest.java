package org.asoem.greyfish.core.genes;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.utils.EvaluatingMarkovChain;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 14:53
 */
public class MarkovGeneTest {
    @Inject
    private Persister persister;
    @Inject
    private GreyfishExpressionFactory factory;

    public MarkovGeneTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final EvaluatingMarkovChain<String> markovChain = EvaluatingMarkovChain.parse("A -> B : 1.0", factory);
        final GreyfishExpression initialState = factory.compile("\"A\"");
        MarkovGeneComponent markovGene = new MarkovGeneComponent(markovChain, initialState);

        // when
        MarkovGeneComponent deserialized = Persisters.createCopy(markovGene, MarkovGeneComponent.class, persister);

        // then
        assertThat(deserialized.getMarkovChain()).isEqualTo(markovChain);
        assertThat(deserialized.getInitialState().getExpression()).isEqualTo(initialState.getExpression());
    }
}
