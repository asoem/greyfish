package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 16:49
 */
public class DoubleGeneTest {

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
        final QuantitativeTrait copy = Persisters.createCopy(doubleGene, JavaPersister.INSTANCE);

        // then
        assertThat(copy).isEqualsToByComparingFields(doubleGene);
    }
}
