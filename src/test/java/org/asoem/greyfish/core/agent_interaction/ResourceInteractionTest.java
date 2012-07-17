package org.asoem.greyfish.core.agent_interaction;

import org.asoem.greyfish.core.actions.ResourceConsumptionAction;
import org.asoem.greyfish.core.actions.ResourceProvisionAction;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInteractionTest {

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        Population population = Population.named("TestPopulation");

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
                .requestAmount(Callbacks.constant(1.0))
                .uptakeUtilization(new Callback<ResourceConsumptionAction, Void>() {
                    @Override
                    public Void apply(ResourceConsumptionAction caller, Arguments arguments) {
                        caller.agent().getProperty("resourceStorage", DoubleProperty.class).add((Double) arguments.get("offer") * 2);
                        return null;
                    }
                })
                .build();


        DoubleProperty resourceProperty = new DoubleProperty.Builder()
                .lowerBound(0.0)
                .upperBound(1.0)
                .initialValue(1.0)
                .build();
        ResourceProvisionAction provisionAction = ResourceProvisionAction.with()
                .name("feed")
                .ontology(messageClassifier)
                .provides(Callbacks.constant(1.0))
                .build();

        Agent consumer = ImmutableAgent.of(population)
                .addProperties(energyStorage)
                .addActions(consumptionAction)
                .build();
        Agent provisioner = ImmutableAgent.of(population)
                .addProperties(resourceProperty)
                .addActions(provisionAction)
                .build();


        final TiledSpace<Agent> space = TiledSpace.<Agent>builder(1, 1).build();
        space.insertObject(consumer, 0, 0, 0);
        space.insertObject(provisioner, 0, 0, 0);

        final Simulation simulation = new ParallelizedSimulation(space);
        Simulations.runFor(simulation, 5);

        // then
        assertThat(energyStorage.getValue()).isEqualTo(2);
    }
}
