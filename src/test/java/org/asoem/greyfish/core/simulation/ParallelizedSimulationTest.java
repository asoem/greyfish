package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.Location2D;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singleton;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ParallelizedSimulationTest {

    @Test
    public void newSimulationTest() {
        // given
        
        final Population population = mock(Population.class);
        given(population.getName()).willReturn("TestPopulation");

        final Agent prototype = ImmutableAgent.of(population).build();
        
        final TiledSpace tiledSpace = mock(TiledSpace.class);
        given(tiledSpace.covers(any(Location2D.class))).willReturn(true);

        // when
        ParallelizedSimulation simulation = new ParallelizedSimulation(tiledSpace, singleton(prototype), ImmutableList.of(prototype, prototype));

        // then
        assertThat(Iterables.size(simulation.getAgents())).isEqualTo(2);
    }
}
