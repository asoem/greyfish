package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 30.07.12
 * Time: 14:13
 */
public class DynamicPropertyTest {

    @Test
    public void testLazyValueComputation() throws Exception {
        // given
        final Callback callback = mock(Callback.class);
        final DynamicProperty<Integer> property = DynamicProperty.<Integer>builder().function(callback).build();
        final Agent agent = mock(Agent.class);
        property.setAgent(agent);
        final Simulation simulation = mock(Simulation.class);
        given(simulation.getStep()).willReturn(0,0,0,1);
        given(agent.simulation()).willReturn(simulation);

        // when
        for (int i = 0; i < 4; i++) {
            property.getValue();
        }

        // then
        verify(callback, times(2)).apply(eq(property), any(Arguments.class));
    }
}
