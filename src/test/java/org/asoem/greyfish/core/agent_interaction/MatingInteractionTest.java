package org.asoem.greyfish.core.agent_interaction;

import org.asoem.greyfish.core.actions.MatingReceiverAction;
import org.asoem.greyfish.core.actions.MatingTransmitterAction;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Callbacks;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.ParallelizedSimulationFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.asoem.greyfish.core.individual.Callbacks.constant;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class MatingInteractionTest {

    @Mock Population population;

    public MatingInteractionTest() {
        //Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    // todo: there is a somewhere a stochastic process involved. The test fails only sometimes.
    @Test
    public void testNormalInteraction() throws Exception {
        // given
        given(population.getName()).willReturn("TestPopulation");

        String messageClassifier = "mate";
        MatingReceiverAction receiverAction = MatingReceiverAction.with()
                .name("receiveSperm")
                .ontology(messageClassifier)
                .interactionRadius(constant(1.0))
                .matingProbability(constant(1.0))
                .build();

        MatingTransmitterAction transmitterAction = MatingTransmitterAction.with()
                .name("sendSperm")
                .ontology(messageClassifier)
                .matingProbability(constant(1.0))
                .build();

        Agent female = ImmutableAgent.of(population)
                .addActions(receiverAction)
                .build();
        Agent male = ImmutableAgent.of(population)
                .addActions(transmitterAction)
                .build();

        final TiledSpace<Agent> space = TiledSpace.ofSize(1, 1);
        space.insertObject(male, 0, 0, 0);
        space.insertObject(female, 0, 0, 0);

        Simulation simulation = new ParallelizedSimulation(space);

        // when
        for (int i = 0; i < 3; ++i) {
            simulation.nextStep();
        }

        // then
        assertThat(receiverAction.getReceivedSperm().size()).isEqualTo(1);
    }
}
