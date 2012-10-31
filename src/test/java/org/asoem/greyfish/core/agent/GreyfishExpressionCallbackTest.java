package org.asoem.greyfish.core.agent;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.core.utils.GreyfishExpressionCallback;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: christoph
 * Date: 04.07.12
 * Time: 15:19
 */
public class GreyfishExpressionCallbackTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;

    public GreyfishExpressionCallbackTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        GreyfishExpressionCallback<Object, Double> callback
                = new GreyfishExpressionCallback<Object, Double>(expressionFactory.compile("1.0"), Double.class);

        // when
        final GreyfishExpressionCallback<Object, Double> copy = Persisters.createCopy(callback, JavaPersister.INSTANCE);

        // then
        assertThat(copy, is(equalTo(callback)));
    }
}
