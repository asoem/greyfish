package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 17:40
 */
public class SuicideTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        Suicide suicide = Suicide.builder()
                .name("foo")
                .executedIf(AlwaysTrueCondition.builder().build())
                .onSuccess(Callbacks.emptyCallback())
                .build();

        // when
        final Suicide copy = Persisters.createCopy(suicide, JavaPersister.INSTANCE);

        // then
        assertThat(copy, is(equalTo(suicide)));
    }
}
