package org.asoem.greyfish.core.agent_interaction;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.actions.ResourceConsumptionAction;
import org.asoem.greyfish.core.actions.ResourceProvisionAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.FrozenAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.core.space.WalledPointSpace;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.space.SpatialObject;
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

        Agent consumer = FrozenAgent.builder(consumerPopulation)
                .addProperties(energyStorage)
                .addActions(consumptionAction)
                .build();
        Agent provisioner = FrozenAgent.builder(providerPopulation)
                .addProperties(resourceProperty)
                .addActions(provisionAction)
                .build();


        final WalledPointSpace<Agent> space = WalledPointSpace.builder(1, 1).build();
        final ImmutableSet<Agent> prototypes = ImmutableSet.of(consumer, provisioner);
        final Simulation<SpatialObject> simulation = ParallelizedSimulation.builder(space, prototypes)
                .agentPool(new StackKeyedObjectPool<Population, Agent>(new BaseKeyedPoolableObjectFactory<Population, Agent>() {
                    final ImmutableMap<Population, Agent> populationPrototypeMap =
                            Maps.uniqueIndex(prototypes, new Function<Agent, Population>() {
                                @Override
                                public Population apply(Agent input) {
                                    return input.getPopulation();
                                }
                            });

                    @Override
                    public Agent makeObject(Population population) throws Exception {
                        return populationPrototypeMap.get(population);
                    }
                }))
                .build();

        simulation.createAgent(consumerPopulation);
        simulation.createAgent(providerPopulation);
        Simulations.runFor(simulation, 6);

        // then
        assertThat(energyStorage.getValue(), is(equalTo(2.0)));
    }
}
