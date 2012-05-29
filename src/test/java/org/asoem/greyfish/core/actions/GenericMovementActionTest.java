package org.asoem.greyfish.core.actions;

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
 * Date: 22.02.12
 * Time: 17:57
 */
public class GenericMovementActionTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public GenericMovementActionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final GenericMovementAction action = GenericMovementAction.builder().stepSize(expressionFactory.compile("0.42")).build();

        // when
        final GenericMovementAction copy = Persisters.createCopy(action, GenericMovementAction.class, persister);

        // then
        assertThat(copy.getStepSize().getExpression()).isEqualTo("0.42");
    }
}
