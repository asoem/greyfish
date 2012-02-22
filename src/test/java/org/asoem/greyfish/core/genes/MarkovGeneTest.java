package org.asoem.greyfish.core.genes;

import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.math.EvaluatingMarkovChain;
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
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final EvaluatingMarkovChain<String> markovChain = EvaluatingMarkovChain.parse("A -> B : 1.0", factory);
        final GreyfishExpression initialState = factory.compile("\"A\"");
        MarkovGene markovGene = new MarkovGene(markovChain, initialState);

        // when
        MarkovGene deserialized = Persisters.createCopy(markovGene, MarkovGene.class, persister);

        // then
        assertThat(deserialized.getMarkovChain()).isEqualTo(markovChain);
        assertThat(deserialized.getInitialState().getExpression()).isEqualTo(initialState.getExpression());
    }
}
