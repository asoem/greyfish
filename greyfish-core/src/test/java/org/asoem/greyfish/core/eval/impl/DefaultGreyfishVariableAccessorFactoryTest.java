package org.asoem.greyfish.core.eval.impl;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 12:03
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultGreyfishVariableAccessorFactoryTest {

    private final DefaultGreyfishVariableAccessorFactory converter = new DefaultGreyfishVariableAccessorFactory();

    @Test
    public void shouldReturnTheContextItselfForAnAction() {
        // given
        final AgentAction<Basic2DAgent> action = mock(AgentAction.class);

        // when
        final Object ret = converter.get("this", AgentAction.class).apply(action);

        // then
        assertEquals(action, ret);
    }

    @Test
    public void shouldReturnTheAgentForAnAction() {
        // given
        final AgentAction<Basic2DAgent> action = mock(AgentAction.class);
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        given(action.agent()).willReturn(Optional.of(agent));

        // when
        final Object ret = converter.get("this.agent", AgentAction.class).apply(action);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnTheSimulationForAnAction() {
        // given
        final AgentAction<Basic2DAgent> action = mock(AgentAction.class);
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final Basic2DSimulation simulation = mock(Basic2DSimulation.class);
        final Optional<BasicSimulationContext<Basic2DSimulation, Basic2DAgent>> contextMockOptional =
                Optional.<BasicSimulationContext<Basic2DSimulation, Basic2DAgent>>of(mock(BasicSimulationContext.class));

        given(action.agent()).willReturn(Optional.of(agent));
        given(agent.getContext()).willReturn(contextMockOptional);
        given(contextMockOptional.get().getSimulation()).willReturn(simulation);

        // when
        final Object ret = converter.get("this.agent.getSimulation", AgentAction.class).apply(action);

        // then
        assertEquals(simulation, ret);
    }

    @Test
    public void shouldReturnTheContextItselfForAProperty() {
        // given
        final AgentProperty<Basic2DAgent, Object> property = mock(AgentProperty.class);

        // when
        final Object ret = converter.get("this", AgentProperty.class).apply(property);

        // then
        assertEquals(property, ret);
    }

    @Test
    public void shouldReturnTheAgentForAProperty() {
        // given
        final AgentProperty<Basic2DAgent, Object> property = mock(AgentProperty.class);
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        given(property.agent()).willReturn(Optional.of(agent));

        // when
        final Object ret = converter.get("this.agent", AgentProperty.class).apply(property);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnAgentsAgeForAnAction() {
        // given
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final BasicSimulationContext<Basic2DSimulation, Basic2DAgent> contextMock = mock(BasicSimulationContext.class);
        final AgentAction<Basic2DAgent> action = mock(AgentAction.class);

        given(agent.getContext()).willReturn(Optional.of(contextMock));
        given(contextMock.getAge()).willReturn(23L);
        given(action.agent()).willReturn(Optional.of(agent));

        // when
        final Object ret = converter.get("this.agent.age", AgentAction.class).apply(action);

        // then
        MatcherAssert.assertThat(ret, is(equalTo((Object)23L)));
    }

    @SuppressWarnings({"NullableProblems"})
    @Test(expected = NullPointerException.class)
    public void shouldScreamIfContextClassIsNull() {
        final AgentAction<Basic2DAgent> action = mock(AgentAction.class);

        converter.get("this", null).apply(action);
    }

    @Test(expected = NullPointerException.class)
    public void shouldScreamIfContextIsNullButRequired() {
        converter.get("this.agent", AgentAction.class).apply(null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldScreamIfExpressionIsEmpty() {
        final AgentAction<Basic2DAgent> action = mock(AgentAction.class);

        converter.get("", AgentAction.class).apply(action);
    }

    @Test(expected = RuntimeException.class)
    public void shouldScreamIfExpressionIsInvalid() {
        final AgentAction<Basic2DAgent> action = mock(AgentAction.class);

        converter.get("fooBar", AgentAction.class).apply(action);
    }
}
