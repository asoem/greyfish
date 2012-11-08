package org.asoem.greyfish.core.agent_interaction;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.actions.FemaleLikeMating;
import org.asoem.greyfish.core.actions.MaleLikeMating;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentInitializers;
import org.asoem.greyfish.core.agent.FrozenAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.space.MotionObject2DImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class MatingInteractionTest {

    public MatingInteractionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        final Population receiverPopulation = Population.named("receiverPopulation");
        final Population donorPopulation = Population.named("donorPopulation");

        String messageClassifier = "mate";
        FemaleLikeMating receiverAction = FemaleLikeMating.with()
                .name("receiveSperm")
                .ontology(messageClassifier)
                .interactionRadius(constant(1.0))
                .matingProbability(constant(1.0))
                .build();

        MaleLikeMating transmitterAction = MaleLikeMating.with()
                .name("sendSperm")
                .ontology(messageClassifier)
                .matingProbability(constant(1.0))
                .build();

        final Agent female = FrozenAgent.builder(receiverPopulation)
                .addActions(receiverAction)
                .build();
        final Agent male = FrozenAgent.builder(donorPopulation)
                .addActions(transmitterAction)
                .build();

        final WalledTileSpace<Agent> space = WalledTileSpace.ofSize(1, 1);
        final ImmutableSet<Agent> prototypes = ImmutableSet.of(male, female);
        final Simulation simulation = ParallelizedSimulation.builder(space, prototypes)
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
        simulation.createAgent(receiverPopulation, AgentInitializers.projection(MotionObject2DImpl.of(0, 0)));
        simulation.createAgent(donorPopulation, AgentInitializers.projection(MotionObject2DImpl.of(0, 0)));
        Simulations.runFor(simulation, 4);

        // then
        assertThat(receiverAction.getReceivedSperm(), hasSize(1));
    }
}
