package org.asoem.greyfish.core.agent;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AgentsTest {
    @Test
    public void testTraitAccessor() throws Exception {
        // given
        final TestAgent agentMock = mock(TestAgent.class);
        final AgentTrait<TestAgent, Double> traitMock = mock(AgentTrait.class);
        final TypeToken<Double> valueType = new TypeToken<Double>() {
        };
        given(traitMock.getName()).willReturn("testTrait");
        given(traitMock.getValueType()).willReturn(valueType);
        given(agentMock.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<TestAgent, ?>>of(traitMock));
        final ComponentAccessor<TestAgent, AgentTrait<TestAgent, Double>> accessor
                = Agents.traitAccessor("testTrait", valueType);

        // when
        final AgentTrait<TestAgent, Double> trait = accessor.apply(agentMock);

        // then
        assertThat(trait, is(traitMock));
    }

    @Test
    public void testTraitAccessorDownCast() throws Exception {
        // given
        final TestAgent agentMock = mock(TestAgent.class);
        final AgentTrait<TestAgent, Double> traitMock = mock(AgentTrait.class);
        given(traitMock.getName()).willReturn("testTrait");
        given(traitMock.getValueType()).willReturn(new TypeToken<Double>() {
        });
        given(agentMock.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<TestAgent, ?>>of(traitMock));
        final ComponentAccessor<TestAgent, AgentTrait<TestAgent, Number>> accessor
                = Agents.traitAccessor("testTrait", new TypeToken<Number>() {
        });

        // when
        final AgentTrait<TestAgent, Number> trait = accessor.apply(agentMock);

        // then
        assertThat(trait, is((Object) traitMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraitAccessorUpCastException() throws Exception {
        // given
        final TestAgent agentMock = mock(TestAgent.class);
        final AgentTrait<TestAgent, Object> traitMock = mock(AgentTrait.class);
        given(traitMock.getName()).willReturn("testTrait");
        given(traitMock.getValueType()).willReturn(new TypeToken<Object>() {
        });
        given(agentMock.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<TestAgent, ?>>of(traitMock));
        final ComponentAccessor<TestAgent, AgentTrait<TestAgent, Number>> accessor
                = Agents.traitAccessor("testTrait", new TypeToken<Number>() {
        });

        // when
        accessor.apply(agentMock);

        // then
        fail();
    }

    private static interface TestAgent extends Agent<TestAgent, SimulationContext<?>> {
    }

    private static interface TestSimulation extends DiscreteTimeSimulation<TestAgent> {
    }
}
