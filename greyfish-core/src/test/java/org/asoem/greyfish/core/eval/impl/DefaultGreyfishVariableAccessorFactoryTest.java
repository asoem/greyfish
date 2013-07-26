package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
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
        final AgentAction<DefaultGreyfishAgent> action = mock(AgentAction.class);

        // when
        final Object ret = converter.get("this", AgentAction.class).apply(action);

        // then
        assertEquals(action, ret);
    }

    @Test
    public void shouldReturnTheAgentForAnAction() {
        // given
        final AgentAction<DefaultGreyfishAgent> action = mock(AgentAction.class);
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        given(action.getAgent()).willReturn(agent);

        // when
        final Object ret = converter.get("this.agent", AgentAction.class).apply(action);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnTheSimulationForAnAction() {
        // given
        final AgentAction<DefaultGreyfishAgent> action = mock(AgentAction.class);
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        final DefaultGreyfishSimulation simulation = mock(DefaultGreyfishSimulation.class);
        given(action.getAgent()).willReturn(agent);
        given(agent.simulation()).willReturn(simulation);

        // when
        final Object ret = converter.get("this.agent.simulation", AgentAction.class).apply(action);

        // then
        assertEquals(simulation, ret);
    }

    @Test
    public void shouldReturnTheContextItselfForAProperty() {
        // given
        final AgentProperty<DefaultGreyfishAgent, Object> property = mock(AgentProperty.class);

        // when
        final Object ret = converter.get("this", AgentProperty.class).apply(property);

        // then
        assertEquals(property, ret);
    }

    @Test
    public void shouldReturnTheAgentForAProperty() {
        // given
        final AgentProperty<DefaultGreyfishAgent, Object> property = mock(AgentProperty.class);
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        given(property.getAgent()).willReturn(agent);

        // when
        final Object ret = converter.get("this.agent", AgentProperty.class).apply(property);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnAgentsAgeForAnAction() {
        // given
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        given(agent.getAge()).willReturn(23);
        final AgentAction<DefaultGreyfishAgent> action = mock(AgentAction.class);
        given(action.getAgent()).willReturn(agent);

        // when
        final Object ret = converter.get("this.agent.age", AgentAction.class).apply(action);

        // then
        MatcherAssert.assertThat(ret, is(equalTo((Object)23)));
    }

    @SuppressWarnings({"NullableProblems"})
    @Test(expected = NullPointerException.class)
    public void shouldScreamIfContextClassIsNull() {
        final AgentAction<DefaultGreyfishAgent> action = mock(AgentAction.class);

        converter.get("this", null).apply(action);
    }

    @Test(expected = NullPointerException.class)
    public void shouldScreamIfContextIsNullButRequired() {
        converter.get("this.agent", AgentAction.class).apply(null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldScreamIfExpressionIsEmpty() {
        final AgentAction<DefaultGreyfishAgent> action = mock(AgentAction.class);

        converter.get("", AgentAction.class).apply(action);
    }

    @Test(expected = RuntimeException.class)
    public void shouldScreamIfExpressionIsInvalid() {
        final AgentAction<DefaultGreyfishAgent> action = mock(AgentAction.class);

        converter.get("fooBar", AgentAction.class).apply(action);
    }
}
