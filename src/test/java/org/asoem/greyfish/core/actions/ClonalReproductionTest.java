package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:28
 */
public class ClonalReproductionTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final ClonalReproduction<DefaultGreyfishAgent> action = ClonalReproduction.<DefaultGreyfishAgent>with()
                .nClones(Callbacks.constant(5))
                .build();
        // when
        final ClonalReproduction<DefaultGreyfishAgent> copy = Persisters.createCopy(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
