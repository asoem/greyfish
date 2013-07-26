package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 19:29
 */
public class GenericMovementTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final GenericMovement<DefaultGreyfishAgent> genericMovement = GenericMovement.<DefaultGreyfishAgent>builder()
                .name("foo")
                .stepSize(Callbacks.constant(1.0))
                .turningAngle(Callbacks.constant(0.42))
                .onSuccess(Callbacks.emptyCallback())
                .executedIf(AlwaysTrueCondition.<DefaultGreyfishAgent>builder().build())
                .build();

        // when
        final GenericMovement<DefaultGreyfishAgent> copy = Persisters.createCopy(genericMovement, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(genericMovement)));
    }
}
