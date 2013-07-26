package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: christoph
 * Date: 16.10.12
 * Time: 14:41
 */
public class QualitativeTraitTest {

    @Test
    public void testMutation() throws Exception {
        // given
        final IdentifierTrait<DefaultGreyfishAgent> discreteTrait = IdentifierTrait.<DefaultGreyfishAgent>builder()
                .name("test")
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
        final IdentifierTrait<DefaultGreyfishAgent> discreteTrait = IdentifierTrait.<DefaultGreyfishAgent>builder()
                .name("test")
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
        final IdentifierTrait<DefaultGreyfishAgent> discreteTrait = IdentifierTrait.<DefaultGreyfishAgent>builder()
                .name("test")
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", Callbacks.constant(1.0))
                .addMutation("b", "c", Callbacks.constant(1.0))
                .build();

        // when
        discreteTrait.set("d");

        // then
        // IllegalArgumentException
    }

    @Test
    public void testGetStates() throws Exception {
        // given
        final IdentifierTrait<DefaultGreyfishAgent> identifierTrait = IdentifierTrait.<DefaultGreyfishAgent>builder()
                .name("test")
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", 1)
                .addMutation("b", "c", 1)
                .addMutation("c", "a", 1)
                .build();

        // when
        final Set<String> states = identifierTrait.getPossibleValues();

        // then
        assertThat(states, containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final IdentifierTrait<DefaultGreyfishAgent> discreteTrait = IdentifierTrait.<DefaultGreyfishAgent>builder()
                .name("test")
                .initialization(Callbacks.constant("foo"))
                .segregation(Callbacks.constant("bar"))
                .addMutation("a", "b", Callbacks.constant(3.0))
                .build();
        discreteTrait.set("a");
        //final Agent agent = mock(Agent.class, withSettings().serializable());
        //discreteTrait.setAgent(agent);

        // when
        final IdentifierTrait<DefaultGreyfishAgent> copy = Persisters.createCopy(discreteTrait, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(discreteTrait)));
    }
}
