package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 14:41
 */
public class DiscreteTraitTest {

    @Test
    public void testMutation() throws Exception {
        // given
        DiscreteTrait<DefaultGreyfishAgent> discreteTrait = DiscreteTrait.<DefaultGreyfishAgent>builder()
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", Callbacks.constant(1.0))
                .addMutation("b", "c", Callbacks.constant(1.0))
                .build();

        // when
        final String mutated1 = discreteTrait.mutate("a");
        final String mutated2 = discreteTrait.mutate("b");
        final String mutated3 = discreteTrait.mutate("c");

        // then
        assertThat(mutated1, is("b"));
        assertThat(mutated2, is("c"));
        assertThat(mutated3, is("c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMutateIllegalArgument() throws Exception {
        // given
        DiscreteTrait<DefaultGreyfishAgent> discreteTrait = DiscreteTrait.<DefaultGreyfishAgent>builder()
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", Callbacks.constant(1.0))
                .addMutation("b", "c", Callbacks.constant(1.0))
                .build();

        // when
        discreteTrait.mutate("d");

        // then
        // IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAlleleIllegalArgument() throws Exception {
        // given
        DiscreteTrait<DefaultGreyfishAgent> discreteTrait = DiscreteTrait.<DefaultGreyfishAgent>builder()
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", Callbacks.constant(1.0))
                .addMutation("b", "c", Callbacks.constant(1.0))
                .build();

        // when
        discreteTrait.setAllele("d");

        // then
        // IllegalArgumentException
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final DiscreteTrait<DefaultGreyfishAgent> discreteTrait = DiscreteTrait.<DefaultGreyfishAgent>builder()
                .name("test")
                .initialization(Callbacks.constant("foo"))
                .segregation(Callbacks.constant("bar"))
                .addMutation("a", "b", Callbacks.constant(3.0))
                .build();
        discreteTrait.setAllele("a");
        //final Agent agent = mock(Agent.class, withSettings().serializable());
        //discreteTrait.setAgent(agent);

        // when
        final DiscreteTrait<DefaultGreyfishAgent> copy = Persisters.createCopy(discreteTrait, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(discreteTrait)));
    }
}
