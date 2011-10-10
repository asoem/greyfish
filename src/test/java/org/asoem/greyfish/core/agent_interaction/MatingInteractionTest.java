package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.actions.MatingReceiverAction;
import org.asoem.greyfish.core.actions.MatingTransmitterAction;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Coordinates2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class MatingInteractionTest {

    @Mock Population population;

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        given(population.getName()).willReturn("TestPopulation");
        EvaluatedGenomeStorage genomeStorage = EvaluatedGenomeStorage.with().name("spermStorage").build();

        String messageClassifier = "mate";
        MatingReceiverAction receiverAction = spy(MatingReceiverAction.with()
                .name("receiveSperm")
                .classification(messageClassifier)
                .searchRadius(1.0)
                .spermStorage(genomeStorage)
                .build());

        MatingTransmitterAction transmitterAction = spy(MatingTransmitterAction.with()
                .name("sendSperm")
                .classification(messageClassifier)
                .build());

        Agent female = spy(ImmutableAgent.of(population)
                .addProperties(genomeStorage)
                .addActions(receiverAction)
                .build());
        Agent male = spy(ImmutableAgent.of(population)
                .addActions(transmitterAction)
                .build());

        Simulation simulationSpy = spy(new ParallelizedSimulation(BasicScenario.builder("TestScenario", TiledSpace.ofSize(0,0)).build()));
        given(simulationSpy.getAgents()).willReturn(ImmutableList.<Agent>of(male,female));
        doReturn(ImmutableList.<MovingObject2D>builder().add(female, male).build()).when(simulationSpy).findObjects(Matchers.<Coordinates2D>any(), anyDouble());

        receiverAction.setAgent(female);
        transmitterAction.setAgent(male);

        female.prepare(simulationSpy);
        male.prepare(simulationSpy);

        // when
        for (int i = 0; i < 3; ++i) {
            female.execute();
            male.execute();
            simulationSpy.step();
        }

        // then
        assertThat(genomeStorage.get().size()).isEqualTo(1);
    }
}
