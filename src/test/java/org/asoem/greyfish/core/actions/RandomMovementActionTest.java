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
 * Time: 17:57
 */
public class RandomMovementActionTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public RandomMovementActionTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final RandomMovementAction action = RandomMovementAction.builder().speed(expressionFactory.compile("0.42")).build();

        // when
        final RandomMovementAction copy = Persisters.createCopy(action, RandomMovementAction.class, persister);

        // then
        assertThat(copy.getSpeed().getExpression()).isEqualTo("0.42");
    }
}
