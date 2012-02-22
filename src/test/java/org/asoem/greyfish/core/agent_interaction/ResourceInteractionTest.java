package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.actions.ResourceConsumptionAction;
import org.asoem.greyfish.core.actions.ResourceProvisionAction;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.core.properties.DoubleProperty;
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
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInteractionTest {
    @Mock
    Population population;

    public ResourceInteractionTest() {
        CoreInjectorHolder.coreInjector();
    }

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        given(population.getName()).willReturn("TestPopulation");

        String messageClassifier = "mate";

        DoubleProperty energyStorage = DoubleProperty.with()
                .name("resourceStorage")
                .lowerBound(0.0)
                .upperBound(2.0)
                .initialValue(0.0)
                .build();
        ResourceConsumptionAction consumptionAction = ResourceConsumptionAction.with()
                .name("eat")
                .ontology(messageClassifier)
                .requestAmount(GreyfishExpressionFactoryHolder.compile("1.0"))
                .uptakeUtilization(GreyfishExpressionFactoryHolder.compile("$('this.agent.properties[\"resourceStorage\"]').add(offer * 2)"))
                .build();


        DoubleProperty resourceProperty = new DoubleProperty.Builder()
                .lowerBound(0.0)
                .upperBound(1.0)
                .initialValue(1.0)
                .build();
        ResourceProvisionAction provisionAction = ResourceProvisionAction.with()
                .name("feed")
                .ontology(messageClassifier)
                .resourceProperty(resourceProperty)
                .build();

        Agent consumer = spy(ImmutableAgent.of(population)
                .addProperties(energyStorage)
                .addActions(consumptionAction)
                .build());
        Agent provisioner = spy(ImmutableAgent.of(population)
                .addProperties(resourceProperty)
                .addActions(provisionAction)
                .build());

        Simulation simulationSpy = spy(new ParallelizedSimulation(BasicScenario.builder("TestScenario", TiledSpace.ofSize(0,0)).build()));
        given(simulationSpy.getAgents()).willReturn(ImmutableList.<Agent>of(provisioner, consumer));
        doReturn(ImmutableList.of(consumer)).when(simulationSpy).findNeighbours(eq(provisioner), anyDouble());
        doReturn(ImmutableList.of(provisioner)).when(simulationSpy).findNeighbours(eq(consumer), anyDouble());

        consumptionAction.setAgent(consumer);
        provisionAction.setAgent(provisioner);

        consumer.prepare(simulationSpy);
        provisioner.prepare(simulationSpy);

        // when
        for (int i = 0; i < 5; ++i) {
            consumer.execute();
            provisioner.execute();
            simulationSpy.step();
        }

        // then
        assertThat(energyStorage.get()).isEqualTo(2);
    }
}
