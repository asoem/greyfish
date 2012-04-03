package org.asoem.greyfish.core.properties;

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
 * Date: 23.09.11
 * Time: 13:19
 */
public class ExpressionPropertyTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public ExpressionPropertyTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final ExpressionProperty expressionProperty = new ExpressionProperty();
        expressionProperty.setExpression(expressionFactory.compile("1.0"));

        // when
        final ExpressionProperty persistentGene = Persisters.createCopy(expressionProperty, ExpressionProperty.class, persister);

        // then
        assertThat(persistentGene.getExpression().getExpression()).isEqualTo("1.0");
    }
}
