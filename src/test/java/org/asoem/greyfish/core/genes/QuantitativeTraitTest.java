package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
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
        final QuantitativeTrait<DefaultGreyfishAgent> quantitativeTrait = QuantitativeTrait.<DefaultGreyfishAgent>builder()
                .name("Test")
                .initialization(Callbacks.constant(1.0))
                .mutation(Callbacks.constant(2.0))
                .segregation(Callbacks.constant(3.0))
                .build();
        quantitativeTrait.set(4.0);
        //final Agent agent = mock(Agent.class, withSettings().serializable());
        //quantitativeTrait.setAgent(agent);

        // when
        final QuantitativeTrait<DefaultGreyfishAgent> copy = Persisters.createCopy(quantitativeTrait, Persisters.javaSerialization());

        // then
        MatcherAssert.assertThat(copy, is(equalTo(quantitativeTrait)));
    }
}
