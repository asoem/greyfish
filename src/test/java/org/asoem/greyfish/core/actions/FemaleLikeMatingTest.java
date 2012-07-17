package org.asoem.greyfish.core.actions;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:40
 */
public class FemaleLikeMatingTest {
    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public FemaleLikeMatingTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }
    
    @Test
    public void testPersistence() throws Exception {
        // given
        final FemaleLikeMating action = FemaleLikeMating.with()
                .ontology("foo")
                .matingProbability(constant(0.42))
                .interactionRadius(constant(0.42))
                .build();

        // when
        final FemaleLikeMating copy = Persisters.createCopy(action, FemaleLikeMating.class, persister);

        // then
        assertThat(copy.getOntology()).isEqualTo("foo");
        assertThat(copy.getMatingProbability()).isEqualTo(constant(0.42));
        assertThat(copy.getInteractionRadius()).isEqualTo(constant(0.42));
    }
}
