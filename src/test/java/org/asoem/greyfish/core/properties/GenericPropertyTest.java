package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 05.04.13
 * Time: 15:26
 */
public class GenericPropertyTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testDefaultLifetimeExpiration() throws Exception {
        // given
        final Callback<GenericProperty<DefaultGreyfishAgent, Object>, Object> function = mock(Callback.class);
        given(function.apply(any(GenericProperty.class), any(Arguments.class))).willReturn(mock(Object.class));
        final GenericProperty<DefaultGreyfishAgent, Object> lifetimeProperty = GenericProperty.<Object, DefaultGreyfishAgent>builder()
                .valueCallback(function)
                .expirationCallback(GenericProperty.<DefaultGreyfishAgent, Object>expiresAtBirth())
                .build();
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        given(agent.getSimulationStep()).willReturn(0, 0, 1, 1);
        lifetimeProperty.setAgent(agent);

        // when
        lifetimeProperty.getValue();
        lifetimeProperty.getValue();

        // then
        verify(function, times(1)).apply(any(GenericProperty.class), any(Arguments.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStepExpiration() throws Exception {
        // given
        final Callback<GenericProperty<DefaultGreyfishAgent, Object>, Object> function = mock(Callback.class);
        given(function.apply(any(GenericProperty.class), any(Arguments.class))).willReturn(mock(Object.class));
        final GenericProperty<DefaultGreyfishAgent, Object> lifetimeProperty = GenericProperty.<Object, DefaultGreyfishAgent>builder()
                .valueCallback(function)
                .expirationCallback(GenericProperty.<DefaultGreyfishAgent, Object>expiresEveryStep())
                .build();
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        given(agent.getSimulationStep()).willReturn(0, 0, 1, 1);
        lifetimeProperty.setAgent(agent);

        // when
        lifetimeProperty.getValue();
        lifetimeProperty.getValue();

        // then
        verify(function, times(2)).apply(any(GenericProperty.class), any(Arguments.class));
    }
}
