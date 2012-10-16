package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 14:05
 */
public class QuantitativeTraitTest {
    @Test
    public void testSerialization() throws Exception {
        final QuantitativeTrait quantitativeTrait = QuantitativeTrait.builder()
                .name("Test")
                .initialization(Callbacks.constant(1.0))
                .mutation(Callbacks.constant(2.0))
                .segregation(Callbacks.constant(3.0))
                .build();

        final QuantitativeTrait copy = Persisters.createCopy(quantitativeTrait, QuantitativeTrait.class, JavaPersister.INSTANCE);

        assertThat(copy.getName()).isEqualTo(quantitativeTrait.getName());
        assertThat(copy.getInitializationKernel()).isEqualTo(quantitativeTrait.getInitializationKernel());
        assertThat(copy.getMutationKernel()).isEqualTo(quantitativeTrait.getMutationKernel());
        assertThat(copy.getSegregationKernel()).isEqualTo(quantitativeTrait.getSegregationKernel());
    }
}
