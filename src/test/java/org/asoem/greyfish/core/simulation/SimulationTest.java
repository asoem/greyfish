package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.singleton;
import static org.asoem.greyfish.core.space.MutableObject2D.at;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class SimulationTest {

    @Mock Agent prototype;
    @Mock Scenario scenario;

    @Test
    public void newSimulationTest() {
        // given
        given(prototype.getComponents()).willReturn(Collections.<AgentComponent>emptyList());
        given(scenario.getSpace()).willReturn(new TiledSpace(10, 10));
        given(scenario.getPrototypes()).willReturn(singleton(prototype));
        given(scenario.getPlaceholder()).willReturn(ImmutableList.of(
                Placeholder.newInstance(prototype, at(0,0)),
                Placeholder.newInstance(prototype, at(0,0)))
        );

        // when
        Simulation simulation = Simulation.newSimulation(scenario);

        // then
        assertTrue(simulation.getAgents().size() == 2);
    }
}
