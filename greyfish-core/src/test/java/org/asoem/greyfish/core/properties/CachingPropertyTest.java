package org.asoem.greyfish.core.properties;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.Basic2DAgentContext;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
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
        final Basic2DSimulation simulationMock = mock(Basic2DSimulation.class);
        given(simulationMock.getTime()).willReturn(0L, 0L, 1L, 1L);
        final BasicSimulationContext mock = mock(BasicSimulationContext.class);
        given(mock.getSimulation()).willReturn(simulationMock);
        given(agent.getContext()).willReturn(Optional.<BasicSimulationContext<Basic2DSimulation, Basic2DAgent>>of(mock));
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
        final BasicSimulationContext contextMock = mock(BasicSimulationContext.class);
        given(contextMock.getTime()).willReturn(0L, 0L, 1L, 1L);
        given(agent.getContext()).willReturn(Optional.<BasicSimulationContext<Basic2DSimulation, Basic2DAgent>>of(contextMock));
        lifetimeProperty.setAgent(agent);

        // when
        lifetimeProperty.value(mock(Basic2DAgentContext.class));
        lifetimeProperty.value(mock(Basic2DAgentContext.class));

        // then
        verify(function, times(2)).apply(any(CachingProperty.class), any(Map.class));
    }
}
