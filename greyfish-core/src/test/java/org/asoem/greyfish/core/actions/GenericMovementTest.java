package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class GenericMovementTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final GenericMovement<Basic2DAgent> genericMovement = GenericMovement.<Basic2DAgent>builder()
                .name("foo")
                .stepSize(Callbacks.constant(1.0))
                .turningAngle(Callbacks.constant(0.42))
                .executedIf(AlwaysTrueCondition.<Basic2DAgent>builder().build())
                .build();

        // when
        final GenericMovement<Basic2DAgent> copy = Persisters.copyAsync(genericMovement, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(genericMovement)));
    }
}
