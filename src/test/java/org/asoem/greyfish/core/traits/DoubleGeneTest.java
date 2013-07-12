package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
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
        final AgentTrait<DefaultGreyfishAgent, Double> doubleGene = DoublePrecisionRealNumberTrait.<DefaultGreyfishAgent>builder()
                .name("test")
                .initialization(constant(1.0))
                .mutation(constant(1.0))
                .segregation(callback)
                .build();

        // when
        final AgentTrait<DefaultGreyfishAgent, Double> copy = Persisters.createCopy(doubleGene, Persisters.javaSerialization());

        // then
        MatcherAssert.assertThat(copy, is(equalTo(doubleGene)));
    }
}
