package org.asoem.greyfish.core.individual;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 04.07.12
 * Time: 15:19
 */
public class GreyfishExpressionCallbackTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;

    @Inject
    private Persister persister;

    public GreyfishExpressionCallbackTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        GreyfishExpressionCallback<Object, Double> callback
                = new GreyfishExpressionCallback<Object, Double>(expressionFactory.compile("1.0"), Double.class);

        // when
        final GreyfishExpressionCallback copy = Persisters.createCopy(callback, GreyfishExpressionCallback.class, persister);

        // then
        assertThat(copy).isEqualTo(callback);
    }
}
