package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.Basic2DAgentContext;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class SymbolTraitTest {

    @Test
    public void testMutation() throws Exception {
        // given
        final SymbolTrait<Basic2DAgent, Basic2DAgentContext> discreteTrait = SymbolTrait.<Basic2DAgent, Basic2DAgentContext>builder()
                .name("test")
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", Callbacks.constant(1.0))
                .addMutation("b", "c", Callbacks.constant(1.0))
                .build();

        // when
        final Basic2DAgentContext contextMock = mock(Basic2DAgentContext.class);
        final String mutated1 = discreteTrait.transform(contextMock, "a");
        final String mutated2 = discreteTrait.transform(contextMock, "b");
        final String mutated3 = discreteTrait.transform(contextMock, "c");

        // then
        assertThat(mutated1, is("b"));
        assertThat(mutated2, is("c"));
        assertThat(mutated3, is("c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMutateIllegalArgument() throws Exception {
        // given
        final SymbolTrait<Basic2DAgent, Basic2DAgentContext> discreteTrait = SymbolTrait.<Basic2DAgent, Basic2DAgentContext>builder()
                .name("test")
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", Callbacks.constant(1.0))
                .addMutation("b", "c", Callbacks.constant(1.0))
                .build();
        final Basic2DAgentContext contextMock = mock(Basic2DAgentContext.class);

        // when
        discreteTrait.transform(contextMock, "d");

        // then
        // IllegalArgumentException
    }

    @Test
    public void testGetStates() throws Exception {
        // given
        final SymbolTrait<Basic2DAgent, Basic2DAgentContext> symbolTrait = SymbolTrait.<Basic2DAgent, Basic2DAgentContext>builder()
                .name("test")
                .initialization(Callbacks.constant("a"))
                .addMutation("a", "b", 1)
                .addMutation("b", "c", 1)
                .addMutation("c", "a", 1)
                .build();

        // when
        final Set<String> states = symbolTrait.getPossibleValues();

        // then
        assertThat(states, containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final SymbolTrait<Basic2DAgent, Basic2DAgentContext> discreteTrait = SymbolTrait.<Basic2DAgent, Basic2DAgentContext>builder()
                .name("test")
                .initialization(Callbacks.constant("foo"))
                .segregation(Callbacks.constant("bar"))
                .addMutation("a", "b", Callbacks.constant(3.0))
                .build();
        //discreteTrait.set("a");
        //final Agent agent = mock(Agent.class, withSettings().serializable());
        //discreteTrait.setAgent(agent);

        // when
        final SymbolTrait<Basic2DAgent, Basic2DAgentContext> copy = Persisters.copyAsync(discreteTrait, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(discreteTrait)));
    }
}
