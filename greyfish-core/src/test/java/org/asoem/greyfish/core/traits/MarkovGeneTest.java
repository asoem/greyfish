package org.asoem.greyfish.core.traits;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;


public class MarkovGeneTest {
    @Inject
    private Persister persister;
    @Inject
    private GreyfishExpressionFactory factory;

    public MarkovGeneTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    /*
    @Test
    public void testPersistence() throws Exception {
        // given
        final EvaluatingMarkovChain<String> markovChain = EvaluatingMarkovChain.parse("A -> B : 1.0", factory);
        final Callback<Object, String> initialState = constant("A");
        QualitativeTrait markovGene = new QualitativeTrait(markovChain, initialState);

        // when
        QualitativeTrait deserialized = Persisters.createCopy(markovGene, QualitativeTrait.class, persister);

        // then
        assertThat(deserialized.getMarkovChain()).isEqualTo(markovChain);
        assertThat(deserialized.getInitializationKernel()).isEqualTo(constant("A"));
    }
    */
}
