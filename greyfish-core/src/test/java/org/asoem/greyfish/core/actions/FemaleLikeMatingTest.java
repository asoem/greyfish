package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:40
 */
public class FemaleLikeMatingTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final FemaleLikeMating<DefaultGreyfishAgent> action = FemaleLikeMating.<DefaultGreyfishAgent>with()
                .name("test")
                .ontology("foo")
                .matingProbability(constant(0.42))
                .interactionRadius(constant(0.42))
                .build();

        // when
        final FemaleLikeMating<DefaultGreyfishAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}