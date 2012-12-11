package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import org.asoem.greyfish.core.actions.FemaleLikeMating;
import org.asoem.greyfish.core.actions.MaleLikeMating;
import org.asoem.greyfish.core.agent.AgentInitializers;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgentImpl;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulationImpl;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.core.space.DefaultGreyfishSpaceImpl;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;
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
        FemaleLikeMating<DefaultGreyfishAgent> receiverAction = FemaleLikeMating.<DefaultGreyfishAgent>with()
                .name("receiveSperm")
                .ontology(messageClassifier)
                .interactionRadius(constant(1.0))
                .matingProbability(constant(1.0))
                .build();

        MaleLikeMating<DefaultGreyfishAgent> transmitterAction = MaleLikeMating.<DefaultGreyfishAgent>with()
                .name("sendSperm")
                .ontology(messageClassifier)
                .matingProbability(constant(1.0))
                .build();

        final DefaultGreyfishAgent female = DefaultGreyfishAgentImpl.builder(receiverPopulation)
                .addAction(receiverAction)
                .build();
        final DefaultGreyfishAgent male = DefaultGreyfishAgentImpl.builder(donorPopulation)
                .addAction(transmitterAction)
                .build();

        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1, 1);
        final ImmutableSet<DefaultGreyfishAgent> prototypes = ImmutableSet.of(male, female);
        final DefaultGreyfishSimulation simulation = new DefaultGreyfishSimulationImpl(space, prototypes);

        simulation.createAgent(receiverPopulation, AgentInitializers.<Point2D>projection(ImmutablePoint2D.at(0, 0)));
        simulation.createAgent(donorPopulation, AgentInitializers.<Point2D>projection(ImmutablePoint2D.at(0, 0)));
        Simulations.runFor(simulation, 4);

        // then
        assertThat(receiverAction.getReceivedSperm(), hasSize(1));
    }
}
