package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import org.asoem.greyfish.core.actions.FemaleLikeMating;
import org.asoem.greyfish.core.actions.MaleLikeMating;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgentImpl;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulation;
import org.asoem.greyfish.core.simulation.DefaultGreyfishSimulationImpl;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.core.space.DefaultGreyfishSpaceImpl;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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

        final String messageClassifier = "mate";
        final FemaleLikeMating<DefaultGreyfishAgent> femaleLikeMating = FemaleLikeMating.<DefaultGreyfishAgent>with()
                .name("receiveSperm")
                .ontology(messageClassifier)
                .interactionRadius(constant(1.0))
                .matingProbability(constant(1.0))
                .executedIf(GenericCondition.<DefaultGreyfishAgent>evaluate(Callbacks.iterate(true, false)))
                .build();

        final MaleLikeMating<DefaultGreyfishAgent> maleLikeMating = MaleLikeMating.<DefaultGreyfishAgent>with()
                .name("sendSperm")
                .ontology(messageClassifier)
                .matingProbability(constant(1.0))
                .executedIf(GenericCondition.<DefaultGreyfishAgent>evaluate(Callbacks.iterate(false, true, false)))
                .build();

        final DefaultGreyfishAgent female = DefaultGreyfishAgentImpl.builder(receiverPopulation)
                .addAction(femaleLikeMating)
                .build();
        female.setProjection(ImmutablePoint2D.at(0,0));
        female.initialize();
        final DefaultGreyfishAgent male = DefaultGreyfishAgentImpl.builder(donorPopulation)
                .addAction(maleLikeMating)
                .build();
        male.setProjection(ImmutablePoint2D.at(0,0));
        male.initialize();

        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1, 1);
        final ImmutableSet<DefaultGreyfishAgent> prototypes = ImmutableSet.of(male, female);
        final DefaultGreyfishSimulation simulation = DefaultGreyfishSimulationImpl.builder(space, prototypes).build();

        simulation.addAgent(male);
        simulation.addAgent(female);
        Simulations.proceed(simulation, 4);
        final ActionState maleLikeMatingState = maleLikeMating.getState();
        Simulations.proceed(simulation, 1);
        final ActionState femaleLikeMatingState = femaleLikeMating.getState();

        // then
        assertThat(femaleLikeMating.getReceivedSperm(), hasSize(1));
        assertThat(femaleLikeMatingState, is(equalTo(ActionState.COMPLETED)));
        assertThat(maleLikeMatingState, is(equalTo(ActionState.COMPLETED)));
    }
}
