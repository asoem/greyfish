package org.asoem.greyfish.core.actions;

import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.ExpressionFactory;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:28
 */
public class ClonalReproductionActionTest {

    @Inject
    private Persister persister;

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    
    public ClonalReproductionActionTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final ClonalReproductionAction action = new ClonalReproductionAction();
        action.setnClones(expressionFactory.compile("42"));
        
        // when
        final ClonalReproductionAction copy = Persisters.createCopy(action, ClonalReproductionAction.class, persister);

        // then
        assertThat(copy.getnClones().getExpression()).isEqualTo("42");
    }
}
