package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.Basic2DAgentContext;
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
public class DoublePrecisionRealNumberTraitTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final DoublePrecisionRealNumberTrait<Basic2DAgent, Basic2DAgentContext> quantitativeTrait = DoublePrecisionRealNumberTrait.<Basic2DAgent, Basic2DAgentContext>builder()
                .name("Test")
                .initialization(Callbacks.constant(1.0))
                .mutation(Callbacks.constant(2.0))
                .segregation(Callbacks.constant(3.0))
                .build();
        //quantitativeTrait.set(4.0);
        //final Agent agent = mock(Agent.class, withSettings().serializable());
        //quantitativeTrait.setAgent(agent);

        // when
        final DoublePrecisionRealNumberTrait<Basic2DAgent, Basic2DAgentContext> copy = Persisters.copyAsync(quantitativeTrait, Persisters.javaSerialization());

        // then
        MatcherAssert.assertThat(copy, is(equalTo(quantitativeTrait)));
    }
}
