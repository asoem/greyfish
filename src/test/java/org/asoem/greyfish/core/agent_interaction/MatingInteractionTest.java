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
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        EvaluatedGenomeStorage genomeStorage = EvaluatedGenomeStorage.with().name("spermStorage").build();

        String messageClassifier = "mate";
        MatingReceiverAction receiverAction = MatingReceiverAction.with()
                .name("receiveSperm")
                .ontology(messageClassifier)
                .interactionRadius(Callbacks.constant(1.0))
                .build();

        MatingTransmitterAction transmitterAction = MatingTransmitterAction.with()
                .name("sendSperm")
                .ontology(messageClassifier)
                .build();

        Agent female = ImmutableAgent.of(population)
                .addProperties(genomeStorage)
                .addActions(receiverAction)
                .build();
        Agent male = ImmutableAgent.of(population)
                .addActions(transmitterAction)
                .build();

        final TiledSpace<Agent> space = TiledSpace.ofSize(1, 1);
        space.insertObject(male, 0, 0, 0);
        space.insertObject(female, 0, 0, 0);

        Simulation simulationSpy = ParallelizedSimulation.create(BasicScenario.builder("TestScenario", space).build());

        receiverAction.setAgent(female);
        transmitterAction.setAgent(male);

        female.initialize();
        female.activate(simulationSpy);

        male.initialize();
        male.activate(simulationSpy);

        // when
        for (int i = 0; i < 3; ++i) {
            simulationSpy.nextStep();
        }

        // then
        assertThat(receiverAction.getReceivedSperm().size()).isEqualTo(1);
    }
}
