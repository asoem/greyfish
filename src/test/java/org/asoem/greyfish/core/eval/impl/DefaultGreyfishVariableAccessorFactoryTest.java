package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
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
        final GFAction action = mock(GFAction.class);

        // when
        final Object ret = converter.get("this", GFAction.class).apply(action);

        // then
        assertEquals(action, ret);
    }

    @Test
    public void shouldReturnTheAgentForAnAction() {
        // given
        final GFAction action = mock(GFAction.class);
        final Agent agent = mock(Agent.class);
        given(action.getAgent()).willReturn(agent);

        // when
        Object ret = converter.get("this.agent", GFAction.class).apply(action);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnTheSimulationForAnAction() {
        // given
        final GFAction action = mock(GFAction.class);
        final Agent agent = mock(Agent.class);
        final Simulation simulation = mock(Simulation.class);
        given(action.getAgent()).willReturn(agent);
        given(agent.simulation()).willReturn(simulation);

        // when
        Object ret = converter.get("this.agent.simulation", GFAction.class).apply(action);

        // then
        assertEquals(simulation, ret);
    }

    @Test
    public void shouldReturnTheContextItselfForAProperty() {
        // given
        final GFProperty property = mock(GFProperty.class);

        // when
        Object ret = converter.get("this", GFProperty.class).apply(property);

        // then
        assertEquals(property, ret);
    }

    @Test
    public void shouldReturnTheAgentForAProperty() {
        // given
        final GFProperty property = mock(GFProperty.class);
        final Agent agent = mock(Agent.class);
        given(property.getAgent()).willReturn(agent);

        // when
        Object ret = converter.get("this.agent", GFProperty.class).apply(property);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnAgentsAgeForAnAction() {
        // given
        final Agent agent = mock(Agent.class);
        given(agent.getAge()).willReturn(23);
        final GFAction action = mock(GFAction.class);
        given(action.getAgent()).willReturn(agent);

        // when
        Object ret = converter.get("this.agent.age", GFAction.class).apply(action);

        // then
        assertThat(ret).isInstanceOf(Integer.class);
        assertThat(ret).isEqualTo(23);
    }

    @SuppressWarnings({"NullableProblems"})
    @Test(expected = NullPointerException.class)
    public void shouldScreamIfContextClassIsNull() {
        final GFAction action = mock(GFAction.class);

        converter.get("this", null).apply(action);
    }

    @Test(expected = NullPointerException.class)
    public void shouldScreamIfContextIsNullButRequired() {
        converter.get("this.agent", GFAction.class).apply(null);
    }

    @Test(expected = RuntimeException.class)
    public void shouldScreamIfExpressionIsEmpty() {
        final GFAction action = mock(GFAction.class);

        converter.get("", GFAction.class).apply(action);
    }

    @Test(expected = RuntimeException.class)
    public void shouldScreamIfExpressionIsInvalid() {
        final GFAction action = mock(GFAction.class);

        converter.get("fooBar", GFAction.class).apply(action);
    }
}
