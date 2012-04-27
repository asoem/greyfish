package org.asoem.greyfish.core.genes;

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
 * Time: 16:49
 */
public class DoubleGeneTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public DoubleGeneTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final DoubleGeneComponent doubleGene = new DoubleGeneComponent();
        doubleGene.setName("test");
        doubleGene.setInitialValue(expressionFactory.compile("1.0"));
        doubleGene.setMutation(expressionFactory.compile("rnorm(0.0, 1.0)"));
        doubleGene.setDistanceMetric(expressionFactory.compile("1.0"));

        // when
        final DoubleGeneComponent persistentGene = Persisters.createCopy(doubleGene, DoubleGeneComponent.class, persister);

        // then
        assertThat(persistentGene.getName()).isEqualTo("test");
        assertThat(persistentGene.getInitialValue().getExpression()).isEqualTo("1.0");
        assertThat(persistentGene.getMutation().getExpression()).isEqualTo("rnorm(0.0, 1.0)");
        assertThat(persistentGene.getDistanceMetric().getExpression()).isEqualTo("1.0");
    }
}
