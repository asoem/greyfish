package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singleton;
import static org.asoem.greyfish.utils.space.MutableObject2D.locatedAt;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ParallelizedSimulationTest {

    @Mock Scenario scenario;
    @Mock Population population;

    @Test
    public void newSimulationTest() {
        // given
        given(population.getName()).willReturn("TestPopulation");
        Agent prototype = ImmutableAgent.of(population).build();
        given(scenario.getSpace()).willReturn(new TiledSpace(10, 10));
        given(scenario.getPrototypes()).willReturn(singleton(prototype));
        given(scenario.getPlaceholder()).willReturn(ImmutableList.of(
                Placeholder.newInstance(prototype, locatedAt(0, 0)),
                Placeholder.newInstance(prototype, locatedAt(0, 0)))
        );

        // when
        ParallelizedSimulation simulation = ParallelizedSimulation.newSimulation(scenario);

        // then
        assertEquals(simulation.getAgents().size(), 2);
    }
}
