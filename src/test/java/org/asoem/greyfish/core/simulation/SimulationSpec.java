package org.asoem.greyfish.core.simulation;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.scenario.Scenario;
import org.junit.runner.RunWith;

import java.awt.*;

import static org.asoem.greyfish.core.space.MutableObject2D.at;

@RunWith(JDaveRunner.class)
@SuppressWarnings("unused")
public class SimulationSpec extends Specification<Simulation> {

    public class SimulationBuildFromScenarioWithNPlaceholders {
        final Agent prototype = ImmutableAgent.with().population(Population.newPopulation("TestPop", Color.black)).build();
        final Scenario scenario = Scenario.with().space(1, 1)
                .add(prototype, at())
                .add(prototype, at())
                .build();
        final Simulation simulation = Simulation.newSimulation(scenario);

        public void shouldHaveNAgentsAtStep0() {
             specify(simulation.getAgents().size(), should.equal(2));
        }
    }
}
