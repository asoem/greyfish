package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.Basic2DAgentContext;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 16:49
 */
public class DoubleGeneTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final Callback<Object, Double> callback = constant(1.0);
        final AgentTrait<?, Double> doubleGene = DoublePrecisionRealNumberTrait.<Basic2DAgent, Basic2DAgentContext>builder()
                .name("test")
                .initialization(constant(1.0))
                .mutation(constant(1.0))
                .segregation(callback)
                .build();

        // when
        final AgentTrait<?, Double> copy = Persisters.copyAsync(doubleGene, Persisters.javaSerialization());

        // then
        MatcherAssert.assertThat(copy, is(equalTo((Object) doubleGene)));
    }
}
