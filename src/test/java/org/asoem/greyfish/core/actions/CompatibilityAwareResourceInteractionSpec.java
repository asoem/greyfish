package org.asoem.greyfish.core.actions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.individual.Prototype;
import org.asoem.greyfish.core.properties.BitSetTrait;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ImmutableBitSet;
import org.junit.runner.RunWith;

import java.awt.*;

import static org.asoem.greyfish.core.space.MutableObject2D.at;

/**
 * User: christoph
 * Date: 05.04.11
 * Time: 11:10
 */
@RunWith(JDaveRunner.class)
public class CompatibilityAwareResourceInteractionSpec extends Specification<ContractNetInitiatorAction> {
    public class NormalInteraction {

        final BitSetTrait trait1 = BitSetTrait.with().name("Trait1").initialValue(ImmutableBitSet.valueOf("111111")).build();
        final DoubleProperty energyStorage = DoubleProperty.with().lowerBound(0.0).upperBound(1.0).initialValue(0.0).build();
        final CompatibilityAwareResourceConsumptionAction consumptionAction =
                new CompatibilityAwareResourceConsumptionAction.Builder()
                        .name("eat")
                        .viaMessagesOfType("test")
                        .requesting(1)
                        .storesEnergyIn(energyStorage)
                        .similarityTrait(trait1)
                        .build();
        final Agent consumer = ImmutableAgent.with()
                        .population(Population.newPopulation("TestPop1", Color.black))
                        .addProperties(trait1, energyStorage)
                        .addActions(consumptionAction)
                        .build();

        final BitSetTrait trait = BitSetTrait.with().name("Trait2").initialValue(ImmutableBitSet.valueOf("111000")).build();
        final ResourceProperty resourceProperty = new ResourceProperty.Builder().lowerBound(0.0).upperBound(1.0).initialValue(1.0).build();
        final CompatibilityAwareResourceProvisionAction provisionAction =
                new CompatibilityAwareResourceProvisionAction.Builder()
                        .name("feed")
                        .parameterMessageType("test")
                        .resourceProperty(resourceProperty)
                        .similarityTrait(trait)
                        .build();
        final Agent provider = ImmutableAgent.with()
                        .population(Population.newPopulation("TestPop2", Color.black))
                        .addProperties(trait, resourceProperty)
                        .addActions(provisionAction)
                        .build();

        final Scenario scenario =
                Scenario.with().space(1,1)
                        .add(consumer, at())
                        .add(provider, at())
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

            specify(Iterables.get(Iterables.filter(consumerClone.getProperties(), DoubleProperty.class), 0).get(), should.equal(0.5));
        }

    }
}
