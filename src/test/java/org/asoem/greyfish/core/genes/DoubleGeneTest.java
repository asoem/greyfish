package org.asoem.greyfish.core.genes;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
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
        final Callback<Object, Double> callback = Callbacks.constant(1.0);
        final QuantitativeTrait doubleGene = QuantitativeTrait.builder()
                .name("test")
                .initialization(constant(1.0))
                .mutation(constant(1.0))
                .segregation(callback)
                .build();

        // when
        final QuantitativeTrait persistentGene = Persisters.createCopy(doubleGene, QuantitativeTrait.class, persister);

        // then
        assertThat(persistentGene.getName()).isEqualTo("test");
        assertThat(persistentGene.getInitializationKernel()).isEqualTo(constant(1.0));
        assertThat(persistentGene.getMutationKernel()).isEqualTo(constant(1.0));
    }
}
