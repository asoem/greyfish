package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:49
 */
public class MaleLikeMatingTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final MaleLikeMating action = MaleLikeMating.with()
                .name("foo")
                .executedIf(AlwaysTrueCondition.builder().build())
                .onSuccess(Callbacks.emptyCallback())
                .ontology("foo")
                .matingProbability(Callbacks.constant(0.42))
                .build();

        // when
        final MaleLikeMating copy = Persisters.createCopy(action, JavaPersister.INSTANCE);

        // then
        assertThat(copy).isEqualsToByComparingFields(action);
    }
}
