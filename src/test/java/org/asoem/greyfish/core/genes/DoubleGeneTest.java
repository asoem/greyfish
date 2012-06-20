package org.asoem.greyfish.core.genes;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.Callback;
import org.asoem.greyfish.core.individual.Callbacks;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.base.Product2;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.core.individual.Callbacks.constant;
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
        final Callback<Object, Product2<Double, Double>> callback = Callbacks.constant(null);
        final DoubleGeneComponent doubleGene = DoubleGeneComponent.builder()
                .name("test")
                .initialAllele(constant(1.0))
                .mutation(constant(1.0))
                .recombination(callback)
                .build();

        // when
        final DoubleGeneComponent persistentGene = Persisters.createCopy(doubleGene, DoubleGeneComponent.class, persister);

        // then
        assertThat(persistentGene.getName()).isEqualTo("test");
        assertThat(persistentGene.getInitialValue()).isEqualTo(constant(1.0));
        assertThat(persistentGene.getMutation()).isEqualTo(constant(1.0));
    }
}
