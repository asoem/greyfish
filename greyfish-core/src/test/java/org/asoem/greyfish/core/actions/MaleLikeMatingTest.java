package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:49
 */
public class MaleLikeMatingTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final MaleLikeMating<Basic2DAgent> action = MaleLikeMating.<Basic2DAgent>with()
                .name("foo")
                .executedIf(AlwaysTrueCondition.<Basic2DAgent>builder().build())
                .onSuccess(Callbacks.emptyCallback())
                .ontology("foo")
                .matingProbability(Callbacks.constant(0.42))
                .build();

        // when
        final MaleLikeMating<Basic2DAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

        // then

        assertThat(copy, is(equalTo(action)));
    }
}
