package org.asoem.greyfish.core.actions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.MutableObject2D;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.runner.RunWith;

import java.awt.*;

import static org.asoem.greyfish.core.space.MutableObject2D.at;

@RunWith(JDaveRunner.class)
public class ResourceInteractionSpec extends Specification<ContractNetInitiatorAction> {
    public class NormalInteraction {

        final DoubleProperty energyStorage = DoubleProperty.with().lowerBound(0.0).upperBound(1.0).initialValue(0.0).build();
        final ResourceConsumptionAction consumptionAction =
                ResourceConsumptionAction.with().name("eat").viaMessagesOfType("builderTest").requesting(1).storesEnergyIn(energyStorage).build();
        final Agent consumer = ImmutableAgent.with().population(Population.newPopulation("TestPop1", Color.black)).addProperties(energyStorage).addActions(consumptionAction).build();

        final ResourceProperty resourceProperty = new ResourceProperty.Builder().lowerBound(0.0).upperBound(1.0).initialValue(1.0).build();
        final ResourceProvisionAction provisionAction = ResourceProvisionAction.with().name("feed").parameterMessageType("builderTest").resourceProperty(resourceProperty).build();
        final Agent provider = ImmutableAgent.with().population(Population.newPopulation("TestPop2", Color.black)).addProperties(resourceProperty).addActions(provisionAction).build();

        final Scenario scenario = BasicScenario.builder("TestScenario", TiledSpace.ofSize(1, 1))
                .addAgent(consumer, MutableObject2D.locatedAt(0, 0))
                .addAgent(provider, MutableObject2D.locatedAt(0, 0))
                .build();

        final Simulation simulation = Simulation.newSimulation(scenario);

        public void shouldTransferTheCorrectAmount() {
            int stepRequired = 5;
            while (stepRequired-- != 0) {
                simulation.step();
            }

            Agent consumerClone = Iterables.find(simulation.getAgents(), new Predicate<Agent>() {
                @Override
                public boolean apply(Agent agent) {
                    return agent.getPopulation().equals(consumer.getPopulation());
                }
            }, null);
            assert consumerClone != null;

            specify(Iterables.get(Iterables.filter(consumerClone.getProperties(), DoubleProperty.class), 0).get(), should.equal(1.0));
        }

    }
}
