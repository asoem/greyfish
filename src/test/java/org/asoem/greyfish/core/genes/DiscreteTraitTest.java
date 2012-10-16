package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 14:41
 */
public class DiscreteTraitTest {

    @Test
    public void testSerialization() throws Exception {
        // given
        final DiscreteTrait discreteTrait = DiscreteTrait.builder()
                .name("test")
                .initialization(Callbacks.constant("foo"))
                .segregation(Callbacks.constant("bar"))
                .addMutation("a", "b", Callbacks.constant(3.0))
                .build();

        // when
        final DiscreteTrait copy = Persisters.createCopy(discreteTrait, DiscreteTrait.class, JavaPersister.INSTANCE);

        // then
        assertThat(copy.getName()).isEqualTo(discreteTrait.getName());
        assertThat(copy.getInitializationKernel()).isEqualTo(copy.getInitializationKernel());
        assertThat(copy.getSegregationKernel()).isEqualTo(copy.getSegregationKernel());
        assertThat(copy.getMarkovChain()).isEqualTo(copy.getMarkovChain());
    }
}
