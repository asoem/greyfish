package org.asoem.greyfish.core.actions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.individual.Prototype;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.Simulation;
import org.junit.runner.RunWith;

import java.awt.*;

import static org.asoem.greyfish.core.space.MutableObject2D.at;

@RunWith(JDaveRunner.class)
public class ResourceProvisionConsumptionSpec extends Specification<ContractNetInitiatiorAction> {
    public class NormalInteraction {

        final DoubleProperty energyStorage = DoubleProperty.with().lowerBound(0.0).upperBound(1.0).initialValue(0.0).build();
        final ResourceConsumptionAction consumptionAction =
                ResourceConsumptionAction.with().viaMessagesOfType("test").requesting(1).storesEnergyIn(energyStorage).build();
        final Prototype consumer = Prototype.newInstance(Individual.with().population(Population.newPopulation("TestPop1", Color.black)).addProperties(energyStorage).addActions(consumptionAction).build());

        final ResourceProperty resourceProperty = new ResourceProperty.Builder().lowerBound(0.0).upperBound(1.0).initialValue(1.0).build();
        final ResourceProvisionAction provisionAction = ResourceProvisionAction.with().parameterMessageType("test").resourceProperty(resourceProperty).build();
        final Prototype provider = Prototype.newInstance(Individual.with().population(Population.newPopulation("TestPop2", Color.black)).addProperties(resourceProperty).addActions(provisionAction).build());

        final Scenario scenario = Scenario.with().space(1,1)
                .add(consumer, at())
                .add(provider, at())
                .build();

        final Simulation simulation = Simulation.newSimulation(scenario);

        public void shouldTransferTheCorrectAmount() {
            int stepRequired = 3;
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
