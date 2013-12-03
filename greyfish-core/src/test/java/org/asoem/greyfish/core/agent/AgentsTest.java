package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 07.08.13
 * Time: 12:12
 */
public class AgentsTest {
    @Test
    public void testTraitAccessor() throws Exception {
        // given
        final TestAgent agentMock = mock(TestAgent.class);
        final AgentTrait traitMock = mock(AgentTrait.class);
        final TypeToken<Double> valueType = new TypeToken<Double>() {};
        given(traitMock.getValueType()).willReturn(valueType);
        given(agentMock.findTrait(any(Predicate.class))).willReturn(traitMock);
        final ComponentAccessor<TestAgent,AgentTrait<TestAgent,Double>> accessor
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
        final AgentTrait traitMock = mock(AgentTrait.class);
        given(traitMock.getValueType()).willReturn(new TypeToken<Double>() {});
        given(agentMock.findTrait(any(Predicate.class))).willReturn(traitMock);
        final ComponentAccessor<TestAgent, AgentTrait<TestAgent, Number>> accessor
                = Agents.traitAccessor("testTrait", new TypeToken<Number>() {});

        // when
        final AgentTrait<TestAgent, Number> trait = accessor.apply(agentMock);

        // then
        assertThat(trait, is(traitMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraitAccessorUpCastException() throws Exception {
        // given
        final TestAgent agentMock = mock(TestAgent.class);
        final AgentTrait traitMock = mock(AgentTrait.class);
        given(traitMock.getValueType()).willReturn(new TypeToken<Object>() {});
        given(agentMock.findTrait(any(Predicate.class))).willReturn(traitMock);
        final ComponentAccessor<TestAgent, AgentTrait<TestAgent, Number>> accessor
                = Agents.traitAccessor("testTrait", new TypeToken<Number>() {});

        // when
        accessor.apply(agentMock);

        // then
        fail();
    }

    private static interface TestAgent extends Agent<TestAgent, TestSimulation> {}
    private static interface TestSimulation extends DiscreteTimeSimulation<TestAgent> {}
}
