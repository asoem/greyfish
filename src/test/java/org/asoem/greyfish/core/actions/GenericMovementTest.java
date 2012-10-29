package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 19:29
 */
public class GenericMovementTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        GenericMovement genericMovement = GenericMovement.builder()
                .name("foo")
                .stepSize(Callbacks.constant(1.0))
                .turningAngle(Callbacks.constant(0.42))
                .onSuccess(Callbacks.emptyCallback())
                .executedIf(AlwaysTrueCondition.builder().build())
                .build();

        // when
        final GenericMovement copy = Persisters.createCopy(genericMovement, JavaPersister.INSTANCE);

        // then
        assertThat(copy).isEqualsToByComparingFields(genericMovement);
    }
}
