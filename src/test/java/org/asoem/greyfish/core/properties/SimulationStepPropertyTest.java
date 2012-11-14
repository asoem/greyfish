package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.asoem.greyfish.utils.space.SpatialObject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 30.07.12
 * Time: 14:13
 */
public class SimulationStepPropertyTest {

    @Test
    public void testLazyValueComputation() throws Exception {
        // given
        final Callback<Object, Integer> callback = mock(Callback.class);
        final SimulationStepProperty<Integer> property = SimulationStepProperty.<Integer>builder().callback(callback).build();
        final Agent agent = mock(Agent.class);
        property.setAgent(agent);
        property.initialize();
        final Simulation<SpatialObject> simulation = mock(Simulation<SpatialObject>.class);
        given(simulation.getStep()).willReturn(0,0,0,1);
        given(agent.simulation()).willReturn(simulation);

        // when
        for (int i = 0; i < 4; i++) {
            property.getValue();
        }

        // then
        verify(callback, times(2)).apply(eq(property), any(Arguments.class));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        SimulationStepProperty<Integer> property = SimulationStepProperty.<Integer>builder()
                .name("foo")
                .callback(Callbacks.constant(42))
                .build();
        final Agent agent = mock(Agent.class, withSettings().serializable());
        property.setAgent(agent);

        // when
        final SimulationStepProperty copy = Persisters.createCopy(property, JavaPersister.INSTANCE);

        // then
        assertThat(copy.getName(), is(equalTo(property.getName())));
        assertThat(copy.getCallback(), is(equalTo(property.getCallback())));
        assertThat(copy.getAgent(), is(instanceOf(agent.getClass())));
    }
}
