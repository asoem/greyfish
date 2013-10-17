package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableSet;
import org.asoem.greyfish.core.actions.ResourceConsumptionAction;
import org.asoem.greyfish.core.actions.ResourceProvisionAction;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.DefaultBasic2DAgent;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
import org.asoem.greyfish.impl.simulation.DefaultBasic2DSimulation;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.impl.space.DefaultBasicTiled2DSpace;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.SimpleTwoDimTreeFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInteractionIT {

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        final Population consumerPopulation = Population.named("ConsumerPopulation");
        final Population providerPopulation = Population.named("ProviderPopulation");

        final String messageClassifier = "mate";

        final DoubleProperty<Basic2DAgent> energyStorage = DoubleProperty.<Basic2DAgent>with()
                .name("resourceStorage")
                .lowerBound(0.0)
                .upperBound(2.0)
                .initialValue(0.0)
                .build();
        final ResourceConsumptionAction<Basic2DAgent> consumptionAction = ResourceConsumptionAction.<Basic2DAgent>with()
                .name("eat")
                .ontology(messageClassifier)
                .requestAmount(Callbacks.constant(1.0))
                .uptakeUtilization(new Callback<ResourceConsumptionAction<Basic2DAgent>, Void>() {
                    @Override
                    public Void apply(final ResourceConsumptionAction<Basic2DAgent> caller, final Map<String, ?> args) {
                        ((DoubleProperty<Basic2DAgent>) caller.agent().get().getProperty("resourceStorage")).add((Double) args.get("offer") * 2);
                        return null;
                    }
                })
                .executedIf(GenericCondition.<Basic2DAgent>evaluate(Callbacks.iterate(true, false)))
                .build();


        final DoubleProperty<Basic2DAgent> resourceProperty = new DoubleProperty.Builder<Basic2DAgent>()
                .name("test")
                .lowerBound(0.0)
                .upperBound(1.0)
                .initialValue(1.0)
                .build();
        final ResourceProvisionAction<Basic2DAgent> provisionAction = ResourceProvisionAction.<Basic2DAgent>with()
                .name("feed")
                .ontology(messageClassifier)
                .provides(Callbacks.constant(1.0))
                .executedIf(GenericCondition.<Basic2DAgent>evaluate(Callbacks.iterate(false, true, false)))
                .build();

        final Basic2DAgent consumer = DefaultBasic2DAgent.builder(consumerPopulation)
                .addProperties(energyStorage)
                .addAction(consumptionAction)
                .build();
        consumer.initialize();
        final Basic2DAgent provisioner = DefaultBasic2DAgent.builder(providerPopulation)
                .addProperties(resourceProperty)
                .addAction(provisionAction)
                .build();
        provisioner.initialize();


        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, SimpleTwoDimTreeFactory.<Basic2DAgent>newInstance());
        final ImmutableSet<Basic2DAgent> prototypes = ImmutableSet.of(consumer, provisioner);
        final Basic2DSimulation simulation = DefaultBasic2DSimulation.builder(space, prototypes).build();

        simulation.enqueueAddition(consumer, ImmutablePoint2D.at(0, 0));
        simulation.enqueueAddition(provisioner, ImmutablePoint2D.at(0, 0));
        Simulations.proceed(simulation, 5);
        final ActionState provisionActionState = provisionAction.getState();
        Simulations.proceed(simulation, 1);
        final ActionState consumptionActionState = consumptionAction.getState();

        // then
        assertThat(energyStorage.get(), is(equalTo(2.0)));
        assertThat(consumptionActionState, is(equalTo(ActionState.COMPLETED)));
        assertThat(provisionActionState, is(equalTo(ActionState.COMPLETED)));
    }
}
