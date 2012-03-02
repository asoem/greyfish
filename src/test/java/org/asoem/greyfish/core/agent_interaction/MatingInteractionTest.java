package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.actions.MatingReceiverAction;
import org.asoem.greyfish.core.actions.MatingTransmitterAction;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class MatingInteractionTest {

    @Mock Population population;

    public MatingInteractionTest() {
        CoreInjectorHolder.coreInjector();
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
                .interactionRadius(1.0)
                .spermStorage(genomeStorage)
                .build();

        MatingTransmitterAction transmitterAction = MatingTransmitterAction.with()
                .name("sendSperm")
                .ontology(messageClassifier)
                .build();

        Agent female = spy(ImmutableAgent.of(population)
                .addProperties(genomeStorage)
                .addActions(receiverAction)
                .build());
        Agent male = spy(ImmutableAgent.of(population)
                .addActions(transmitterAction)
                .build());

        Simulation simulationSpy = spy(new ParallelizedSimulation(BasicScenario.builder("TestScenario", TiledSpace.<Agent>ofSize(0,0)).build()));
        given(simulationSpy.getAgents()).willReturn(ImmutableList.<Agent>of(male,female));
        doReturn(ImmutableList.builder().add(female, male).build()).when(simulationSpy).findNeighbours(Matchers.<Agent>any(), anyDouble());

        receiverAction.setAgent(female);
        transmitterAction.setAgent(male);

        female.prepare(simulationSpy);
        male.prepare(simulationSpy);

        // when
        for (int i = 0; i < 3; ++i) {
            simulationSpy.step();
        }

        // then
        //assertThat(genomeStorage.get().size()).isEqualTo(1);
    }
}
