package org.asoem.greyfish.core.actions;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.utils.GreyfishExpressionCallback;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:49
 */
public class MaleLikeMatingTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public MaleLikeMatingTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final GreyfishExpressionCallback<MaleLikeMating, Double> matingProbability =
                new GreyfishExpressionCallback<MaleLikeMating, Double>(expressionFactory.compile("0.42"), Double.class);
        final MaleLikeMating action = MaleLikeMating.with()
                .ontology("foo")
                .matingProbability(matingProbability)
                .build();

        // when
        final MaleLikeMating copy = Persisters.createCopy(action, MaleLikeMating.class, persister);

        // then
        assertThat(copy.getOntology()).isEqualTo("foo");
        assertThat(copy.getMatingProbability()).isEqualTo(matingProbability);
    }
}
