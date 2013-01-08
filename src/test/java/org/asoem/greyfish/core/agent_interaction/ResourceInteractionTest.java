package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.actions.ResourceConsumptionAction;
import org.asoem.greyfish.core.actions.ResourceProvisionAction;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgentImpl;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulationImpl;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.core.space.DefaultGreyfishSpaceImpl;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInteractionTest {

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        Population consumerPopulation = Population.named("ConsumerPopulation");
        Population providerPopulation = Population.named("ProviderPopulation");

        String messageClassifier = "mate";

        DoubleProperty<DefaultGreyfishAgent> energyStorage = DoubleProperty.<DefaultGreyfishAgent>with()
                .name("resourceStorage")
                .lowerBound(0.0)
                .upperBound(2.0)
                .initialValue(0.0)
                .build();
        ResourceConsumptionAction<DefaultGreyfishAgent> consumptionAction = ResourceConsumptionAction.<DefaultGreyfishAgent>with()
                .name("eat")
                .ontology(messageClassifier)
                .requestAmount(Callbacks.constant(1.0))
                .uptakeUtilization(new Callback<ResourceConsumptionAction<DefaultGreyfishAgent>, Void>() {
                    @Override
                    public Void apply(ResourceConsumptionAction<DefaultGreyfishAgent> caller, Arguments arguments) {
                        ((DoubleProperty<DefaultGreyfishAgent>) caller.agent().getProperty("resourceStorage")).add((Double) arguments.get("offer") * 2);
                        return null;
                    }
                })
                .build();


        DoubleProperty<DefaultGreyfishAgent> resourceProperty = new DoubleProperty.Builder<DefaultGreyfishAgent>()
                .lowerBound(0.0)
                .upperBound(1.0)
                .initialValue(1.0)
                .build();
        ResourceProvisionAction<DefaultGreyfishAgent> provisionAction = ResourceProvisionAction.<DefaultGreyfishAgent>with()
                .name("feed")
                .ontology(messageClassifier)
                .provides(Callbacks.constant(1.0))
                .build();

        DefaultGreyfishAgent consumer = DefaultGreyfishAgentImpl.builder(consumerPopulation)
                .addProperties(energyStorage)
                .addAction(consumptionAction)
                .build();
        DefaultGreyfishAgent provisioner = DefaultGreyfishAgentImpl.builder(providerPopulation)
                .addProperties(resourceProperty)
                .addAction(provisionAction)
                .build();


        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1,1);
        final ImmutableSet<DefaultGreyfishAgent> prototypes = ImmutableSet.of(consumer, provisioner);
        final DefaultGreyfishSimulation simulation = DefaultGreyfishSimulationImpl.builder(space, prototypes).build();

        simulation.createAgent(consumerPopulation, ImmutablePoint2D.at(0,0));
        simulation.createAgent(providerPopulation, ImmutablePoint2D.at(0,0));
        Simulations.proceed(simulation, 6);

        // then
        assertThat(energyStorage.getValue(), is(equalTo(2.0)));
    }
}
