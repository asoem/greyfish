package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 12:03
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultGreyfishVariableAccessorFactoryTest {

    private final DefaultGreyfishVariableAccessorFactory converter = new DefaultGreyfishVariableAccessorFactory();

    @Mock Agent agent;
    @Mock GFAction action;
    @Mock GFProperty property;
    @Mock ParallelizedSimulation simulation;

    @Test
    public void shouldReturnTheContextItselfForAnAction() {
        // when
        Object ret = converter.get("this", GFAction.class).apply(action);

        // then
        assertEquals(action, ret);
    }

    @Test
    public void shouldReturnTheAgentForAnAction() {
        // given
        given(action.getAgent()).willReturn(agent);

        // when
        Object ret = converter.get("this.agent", GFAction.class).apply(action);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnTheSimulationForAnAction() {
        // given
        given(action.getAgent()).willReturn(agent);
        given(agent.getSimulation()).willReturn(simulation);

        // when
        Object ret = converter.get("this.agent.simulation", GFAction.class).apply(action);

        // then
        assertEquals(simulation, ret);
    }

    @Test
    public void shouldReturnTheContextItselfForAProperty() {
        // when
        Object ret = converter.get("this", GFProperty.class).apply(property);

        // then
        assertEquals(property, ret);
    }

    @Test
    public void shouldReturnTheAgentForAProperty() {
        // given
        given(property.getAgent()).willReturn(agent);

        // when
        Object ret = converter.get("this.agent", GFProperty.class).apply(property);

        // then
        assertEquals(agent, ret);
    }

    @Test
    public void shouldReturnAgentsAgeForAnAction() {
        // given
        given(action.getAgent()).willReturn(agent);
        given(agent.getAge()).willReturn(23);

        // when
        Object ret = converter.get("this.agent.age", GFAction.class).apply(action);

        // then
        assertThat(ret).isInstanceOf(Integer.class);
        assertThat(ret).isEqualTo(23);
    }

    @SuppressWarnings({"NullableProblems"})
    @Test(expected=NullPointerException.class)
    public void shouldScreamIfContextClassIsNull() {
        converter.get("this", null).apply(action);
    }

    @Test(expected=NullPointerException.class)
    public void shouldScreamIfContextIsNullButRequired() {
        converter.get("this.agent", GFAction.class).apply(null);
    }

    @Test(expected=RuntimeException.class)
    public void shouldScreamIfExpressionIsEmpty() {
        converter.get("", GFAction.class).apply(action);
    }

    @Test(expected=RuntimeException.class)
    public void shouldScreamIfExpressionIsInvalid() {
        converter.get("fooBar", GFAction.class).apply(action);
    }
}
