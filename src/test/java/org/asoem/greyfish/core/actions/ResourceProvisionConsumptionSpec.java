package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.runner.RunWith;

import static org.asoem.greyfish.core.space.Location2D.at;

@RunWith(JDaveRunner.class)
public class ResourceProvisionConsumptionSpec extends Specification<ContractNetInitiatiorAction> {
    public class NormalInteraction {

        DoubleProperty energyStorage = DoubleProperty.with().lowerBound(0.0).upperBound(1.0).initialValue(0.0).build();
        ResourceConsumptionAction consumptionAction =
                ResourceConsumptionAction.with().viaMessagesOfType("test").requesting(1).storesEnergyIn(energyStorage).build();
        Individual consumer = Individual.with().population(new Population("TestPop1")).addProperties(energyStorage).addActions(consumptionAction).build();

        ResourceProperty resourceProperty = new ResourceProperty.Builder().lowerBound(0.0).upperBound(1.0).initialValue(1.0).build();
        ResourceProvisionAction provisionAction = ResourceProvisionAction.with().parameterMessageType("test").resourceProperty(resourceProperty).build();
        Individual provider = Individual.with().population(new Population("TestPop2")).addProperties(resourceProperty).addActions(provisionAction).build();

        TiledSpace space = new TiledSpace(1,1);
        Scenario scenario = Scenario.with().space(space)
                .add(consumer, at(0,0))
                .add(provider, at(0,0))
                .build();

        Simulation simulation = new Simulation(scenario);

        public void shouldTransferTheCorrectAmount() {
            int stepRequired = 3;
            while (stepRequired-- != 0) {
                simulation.step();
            }

            specify(Iterables.get(simulation.getIndividuals().get(1).getProperties(DoubleProperty.class), 0).getValue(), should.equal(1.0));
        }

    }
}
