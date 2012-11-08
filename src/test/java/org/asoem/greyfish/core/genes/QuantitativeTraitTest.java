package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 14:05
 */
public class QuantitativeTraitTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final QuantitativeTrait quantitativeTrait = QuantitativeTrait.builder()
                .name("Test")
                .initialization(Callbacks.constant(1.0))
                .mutation(Callbacks.constant(2.0))
                .segregation(Callbacks.constant(3.0))
                .build();
        quantitativeTrait.setAllele(4.0);
        //final Agent agent = mock(Agent.class, withSettings().serializable());
        //quantitativeTrait.setAgent(agent);

        // when
        final QuantitativeTrait copy = Persisters.createCopy(quantitativeTrait, JavaPersister.INSTANCE);

        // then
        MatcherAssert.assertThat(copy, is(equalTo(quantitativeTrait)));
    }
}
