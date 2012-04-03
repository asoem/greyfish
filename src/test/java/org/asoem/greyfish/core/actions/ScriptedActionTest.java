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
 * Time: 18:08
 */
public class ScriptedActionTest {
    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public ScriptedActionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final ScriptedAction action = ScriptedAction.builder().executes(expressionFactory.compile("true")).build();

        // when
        final ScriptedAction copy = Persisters.createCopy(action, ScriptedAction.class, persister);

        // then
        assertThat(copy.getScript().getExpression()).isEqualTo("true");
    }
}
