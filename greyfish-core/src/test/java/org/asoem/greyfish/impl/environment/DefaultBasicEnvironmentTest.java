/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.impl.environment;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DefaultBasicEnvironmentTest {
    @Test
    public void testBuilder() throws Exception {
        // when
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test").build();

        // then
        assertThat(simulation, is(notNullValue()));
    }

    @Test
    public void testBuilderWithExecutor() throws Exception {
        // when
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test")
                .executorService(Executors.newSingleThreadExecutor()).build();

        // then
        assertThat(simulation, is(notNullValue()));
    }

    @Test
    public void testBuilderWithLogger() throws Exception {
        // when
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test")
                .build();

        // then
        assertThat(simulation, is(notNullValue()));
    }

    @Test
    public void testCountAgents() throws Exception {
        // given
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test").build();

        // when
        final int i = simulation.countAgents();

        // then
        assertThat(i, is(0));
    }

    @Test
    public void testAddAgent() throws Exception {
        // given
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test").build();
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
        verify(agentMock).activate(any(BasicContext.class));
    }

    @Test
    public void testRemoveAgent() throws Exception {
        // given
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test").build();
        final BasicAgent agentMock = mock(BasicAgent.class);
        given(agentMock.isActive()).willReturn(false, true);
        final BasicContext<BasicEnvironment, BasicAgent> contextMock = mock(BasicContext.class);
        given(agentMock.getContext()).willReturn(Optional.<BasicContext<BasicEnvironment, BasicAgent>>of(contextMock));
        given(contextMock.getEnvironment()).willReturn(simulation);
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
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test").build();
        final BasicAgent agentMock = mock(BasicAgent.class);
        given(agentMock.isActive()).willReturn(false, true);
        simulation.enqueueAddition(agentMock);
        final ACLMessage<BasicAgent> message = mock(ACLMessage.class);
        given(message.getRecipients()).willReturn(ImmutableSet.of(agentMock));

        // when
        simulation.deliverMessage(message);

        // then
        verify(agentMock, never()).ask(message, Void.class);

        // and when
        simulation.nextStep();

        // then
        verify(agentMock).ask(message, Void.class);
    }

    @Test
    public void testEventPublisher() throws Exception {
        // given
        EventBus eventPublisherMock = mock(EventBus.class);
        final DefaultBasicEnvironment simulation = DefaultBasicEnvironment.builder("test")
                .eventBus(eventPublisherMock)
                .build();
        final BasicAgent agentMock = mock(BasicAgent.class);
        final BasicContext<BasicEnvironment, BasicAgent> contextMock = mock(BasicContext.class);
        given(agentMock.getContext()).willReturn(Optional.<BasicContext<BasicEnvironment, BasicAgent>>of(contextMock));
        given(contextMock.getEnvironment()).willReturn(simulation);
        given(agentMock.isActive()).willReturn(false, true, true, false);

        // when
        simulation.enqueueAddition(agentMock);
        simulation.nextStep();

        // then
        verify(eventPublisherMock).post(argThat(Matchers.isA(AgentAddedEvent.class)));
        verify(eventPublisherMock).post(argThat(Matchers.isA(TimeChangedEvent.class)));

        // and when
        simulation.enqueueRemoval(agentMock);
        simulation.nextStep();

        // then
        verify(eventPublisherMock).post(argThat(Matchers.isA(AgentRemovedEvent.class)));
        verify(eventPublisherMock, times(2)).post(argThat(Matchers.isA(TimeChangedEvent.class)));
    }
}
