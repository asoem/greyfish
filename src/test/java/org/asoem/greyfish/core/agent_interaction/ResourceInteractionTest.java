package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.actions.ResourceConsumptionAction;
import org.asoem.greyfish.core.actions.ResourceProvisionAction;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgentImpl;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.conditions.GenericCondition;
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
        final Population consumerPopulation = Population.named("ConsumerPopulation");
        final Population providerPopulation = Population.named("ProviderPopulation");

        final String messageClassifier = "mate";

        final DoubleProperty<DefaultGreyfishAgent> energyStorage = DoubleProperty.<DefaultGreyfishAgent>with()
                .name("resourceStorage")
                .lowerBound(0.0)
                .upperBound(2.0)
                .initialValue(0.0)
                .build();
        final ResourceConsumptionAction<DefaultGreyfishAgent> consumptionAction = ResourceConsumptionAction.<DefaultGreyfishAgent>with()
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
                .executedIf(GenericCondition.<DefaultGreyfishAgent>evaluate(Callbacks.iterate(true, false)))
                .build();


        final DoubleProperty<DefaultGreyfishAgent> resourceProperty = new DoubleProperty.Builder<DefaultGreyfishAgent>()
                .lowerBound(0.0)
                .upperBound(1.0)
                .initialValue(1.0)
                .build();
        final ResourceProvisionAction<DefaultGreyfishAgent> provisionAction = ResourceProvisionAction.<DefaultGreyfishAgent>with()
                .name("feed")
                .ontology(messageClassifier)
                .provides(Callbacks.constant(1.0))
                .executedIf(GenericCondition.<DefaultGreyfishAgent>evaluate(Callbacks.iterate(false, true, false)))
                .build();

        final DefaultGreyfishAgent consumer = DefaultGreyfishAgentImpl.builder(consumerPopulation)
                .addProperties(energyStorage)
                .addAction(consumptionAction)
                .build();
        consumer.setProjection(ImmutablePoint2D.at(0,0));
        consumer.initialize();
        final DefaultGreyfishAgent provisioner = DefaultGreyfishAgentImpl.builder(providerPopulation)
                .addProperties(resourceProperty)
                .addAction(provisionAction)
                .build();
        provisioner.setProjection(ImmutablePoint2D.at(0,0));
        provisioner.initialize();


        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1,1);
        final ImmutableSet<DefaultGreyfishAgent> prototypes = ImmutableSet.of(consumer, provisioner);
        final DefaultGreyfishSimulation simulation = DefaultGreyfishSimulationImpl.builder(space, prototypes).build();

        simulation.addAgent(consumer);
        simulation.addAgent(provisioner);
        Simulations.proceed(simulation, 4);
        final ActionState provisionActionState = provisionAction.getState();
        Simulations.proceed(simulation, 1);
        final ActionState consumptionActionState = consumptionAction.getState();

        // then
        assertThat(energyStorage.getValue(), is(equalTo(2.0)));
        assertThat(consumptionActionState, is(equalTo(ActionState.COMPLETED)));
        assertThat(provisionActionState, is(equalTo(ActionState.COMPLETED)));
    }
}
