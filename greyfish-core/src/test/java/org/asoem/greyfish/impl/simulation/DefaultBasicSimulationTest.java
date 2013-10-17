package org.asoem.greyfish.impl.simulation;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.utils.DiscreteTimeListener;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.junit.Test;

import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DefaultBasicSimulationTest {
    @Test
    public void testBuilder() throws Exception {
        // when
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test").build();

        // then
        assertThat(simulation, is(notNullValue()));
    }

    @Test
    public void testBuilderWithExecutor() throws Exception {
        // when
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test")
                .executorService(Executors.newSingleThreadExecutor()).build();

        // then
        assertThat(simulation, is(notNullValue()));
    }

    @Test
    public void testBuilderWithLogger() throws Exception {
        // when
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test")
                .logger(SimulationLoggers.nullLogger()).build();

        // then
        assertThat(simulation, is(notNullValue()));
    }

    @Test
    public void testCountAgents() throws Exception {
        // given
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test").build();

        // when
        final int i = simulation.countAgents();

        // then
        assertThat(i, is(0));
    }

    @Test
    public void testAddAgent() throws Exception {
        // given
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test").build();
        final BasicAgent agentMock = mock(BasicAgent.class);
        given(agentMock.isActive()).willReturn(false, true);

        // when
        simulation.enqueueAddition(agentMock);
        final int agentCountBeforeStep = simulation.countAgents();
        simulation.nextStep();
        final int agentCountAfterStep = simulation.countAgents();

        // then
        assertThat(agentCountBeforeStep, is(0));
        assertThat(agentCountAfterStep, is(1));
        verify(agentMock).activate(any(SimulationContext.class));
    }

    @Test
    public void testRemoveAgent() throws Exception {
        // given
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test").build();
        final BasicAgent agentMock = mock(BasicAgent.class);
        given(agentMock.isActive()).willReturn(false, true);
        given(agentMock.simulation()).willReturn(simulation);
        simulation.enqueueAddition(agentMock);
        simulation.nextStep();
        checkState(simulation.countAgents() == 1);
        given(agentMock.isActive()).willReturn(true, false);


        // when
        simulation.enqueueRemoval(agentMock);
        final int agentCountBeforeStep = simulation.countAgents();
        simulation.nextStep();
        final int agentCountAfterStep = simulation.countAgents();

        // then
        assertThat(agentCountBeforeStep, is(1));
        assertThat(agentCountAfterStep, is(0));
        verify(agentMock).deactivate();
    }

    @Test
    public void testDeliverMessage() throws Exception {
        // given
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test").build();
        final BasicAgent agentMock = mock(BasicAgent.class);
        given(agentMock.isActive()).willReturn(false, true);
        simulation.enqueueAddition(agentMock);
        final ACLMessage<BasicAgent> message = mock(ACLMessage.class);
        given(message.getRecipients()).willReturn(ImmutableSet.of(agentMock));

        // when
        simulation.deliverMessage(message);

        // then
        verify(agentMock, never()).receive(message);

        // and when
        simulation.nextStep();

        // then
        verify(agentMock).receive(message);
    }

    @Test
    public void testAddTimeChangeListener() throws Exception {
        // given
        final DefaultBasicSimulation simulation = DefaultBasicSimulation.builder("test").build();
        final DiscreteTimeListener timeListenerMock = mock(DiscreteTimeListener.class);
        final long time = simulation.getTime();

        // when
        simulation.addTimeChangeListener(timeListenerMock);
        simulation.nextStep();

        // then
        verify(timeListenerMock).timeChanged(simulation, time, time+1);
    }
}
