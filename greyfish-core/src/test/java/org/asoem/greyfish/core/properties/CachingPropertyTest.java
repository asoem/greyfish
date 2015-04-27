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

package org.asoem.greyfish.core.properties;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.Basic2DAgentContext;
import org.asoem.greyfish.impl.environment.Basic2DEnvironment;
import org.asoem.greyfish.utils.base.Callback;
import org.junit.Test;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


public class CachingPropertyTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testDefaultLifetimeExpiration() throws Exception {
        // given
        final Callback<CachingProperty<Basic2DAgent, Object, Basic2DAgentContext>, Object> function = mock(Callback.class);
        given(function.apply(any(CachingProperty.class), any(Map.class))).willReturn(mock(Object.class));
        final CachingProperty<Basic2DAgent, Object, Basic2DAgentContext> lifetimeProperty = CachingProperty.<Object, Basic2DAgent, Basic2DAgentContext>builder()
                .name("")
                .value(function)
                .expires(CachingProperty.expiresAtBirth())
                .build();
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final Basic2DEnvironment simulationMock = mock(Basic2DEnvironment.class);
        given(simulationMock.getTime()).willReturn(0L, 0L, 1L, 1L);
        final BasicContext mock = mock(BasicContext.class);
        given(mock.getEnvironment()).willReturn(simulationMock);
        given(agent.getContext()).willReturn(Optional.<BasicContext<Basic2DEnvironment, Basic2DAgent>>of(mock));
        lifetimeProperty.setAgent(agent);

        // when
        lifetimeProperty.value(mock(Basic2DAgentContext.class));
        lifetimeProperty.value(mock(Basic2DAgentContext.class));

        // then
        verify(function, times(1)).apply(any(CachingProperty.class), any(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStepExpiration() throws Exception {
        // given
        final Callback<CachingProperty<Basic2DAgent, Object, Basic2DAgentContext>, Object> function = mock(Callback.class);
        given(function.apply(any(CachingProperty.class), any(Map.class))).willReturn(mock(Object.class));
        final CachingProperty<Basic2DAgent, Object, Basic2DAgentContext> lifetimeProperty = CachingProperty.<Object, Basic2DAgent, Basic2DAgentContext>builder()
                .name("")
                .value(function)
                .expires(CachingProperty.expiresEveryStep())
                .build();
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final BasicContext contextMock = mock(BasicContext.class);
        given(contextMock.getTime()).willReturn(0L, 0L, 1L, 1L);
        given(agent.getContext()).willReturn(Optional.<BasicContext<Basic2DEnvironment, Basic2DAgent>>of(contextMock));
        lifetimeProperty.setAgent(agent);

        // when
        lifetimeProperty.value(mock(Basic2DAgentContext.class));
        lifetimeProperty.value(mock(Basic2DAgentContext.class));

        // then
        verify(function, times(2)).apply(any(CachingProperty.class), any(Map.class));
    }
}
