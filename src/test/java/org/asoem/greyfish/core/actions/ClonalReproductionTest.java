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
 * Time: 17:28
 */
public class ClonalReproductionTest {

    @Inject
    private Persister persister;

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    
    public ClonalReproductionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final ClonalReproduction action = new ClonalReproduction();
        action.setnClones(expressionFactory.compile("42"));
        
        // when
        final ClonalReproduction copy = Persisters.createCopy(action, ClonalReproduction.class, persister);

        // then
        assertThat(copy.getnClones().getExpression()).isEqualTo("42");
    }
}
