package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.individual.Prototype;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.Simulation;
import org.junit.runner.RunWith;

import java.awt.*;

import static org.asoem.greyfish.core.space.Location2D.at;

@RunWith(JDaveRunner.class)
public class ResourceProvisionConsumptionSpec extends Specification<ContractNetInitiatiorAction> {
    public class NormalInteraction {

        DoubleProperty energyStorage = DoubleProperty.with().lowerBound(0.0).upperBound(1.0).initialValue(0.0).build();
        ResourceConsumptionAction consumptionAction =
                ResourceConsumptionAction.with().viaMessagesOfType("test").requesting(1).storesEnergyIn(energyStorage).build();
        Prototype consumer = Prototype.newInstance(Individual.with().population(Population.newPopulation("TestPop1", Color.black)).addProperties(energyStorage).addActions(consumptionAction).build());

        ResourceProperty resourceProperty = new ResourceProperty.Builder().lowerBound(0.0).upperBound(1.0).initialValue(1.0).build();
        ResourceProvisionAction provisionAction = ResourceProvisionAction.with().parameterMessageType("test").resourceProperty(resourceProperty).build();
        Prototype provider = Prototype.newInstance(Individual.with().population(Population.newPopulation("TestPop2", Color.black)).addProperties(resourceProperty).addActions(provisionAction).build());

        Scenario scenario = Scenario.with().space(1,1)
                .add(consumer, at(0,0))
                .add(provider, at(0,0))
                .build();

        Simulation simulation = Simulation.newSimulation(scenario);

        public void shouldTransferTheCorrectAmount() {
            int stepRequired = 3;
            while (stepRequired-- != 0) {
                simulation.step();
            }

            specify(Iterables.get(simulation.getAgents().get(1).getProperties(DoubleProperty.class), 0).getValue(), should.equal(1.0));
        }

    }
}
