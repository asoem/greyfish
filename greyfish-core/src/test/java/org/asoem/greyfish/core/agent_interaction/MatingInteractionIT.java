package org.asoem.greyfish.core.agent_interaction;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import org.asoem.greyfish.core.actions.FemaleLikeMating;
import org.asoem.greyfish.core.actions.MaleLikeMating;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.simulation.Simulations;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.DefaultBasic2DAgent;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
import org.asoem.greyfish.impl.simulation.DefaultBasic2DSimulation;
import org.asoem.greyfish.impl.space.DefaultGreyfishTiled2DSpace;
import org.asoem.greyfish.impl.space.DefaultGreyfishTiled2DSpaceImpl;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class MatingInteractionIT {

    public MatingInteractionIT() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        final Population receiverPopulation = Population.named("receiverPopulation");
        final Population donorPopulation = Population.named("donorPopulation");

        final String messageClassifier = "mate";
        final FemaleLikeMating<Basic2DAgent> femaleLikeMating = FemaleLikeMating.<Basic2DAgent>with()
                .name("receiveSperm")
                .ontology(messageClassifier)
                .interactionRadius(constant(1.0))
                .matingProbability(constant(1.0))
                .executedIf(GenericCondition.<Basic2DAgent>evaluate(Callbacks.iterate(true, false)))
                .build();

        final MaleLikeMating<Basic2DAgent> maleLikeMating = MaleLikeMating.<Basic2DAgent>with()
                .name("sendSperm")
                .ontology(messageClassifier)
                .matingProbability(constant(1.0))
                .executedIf(GenericCondition.<Basic2DAgent>evaluate(Callbacks.iterate(false, true, false)))
                .build();

        final Basic2DAgent female = DefaultBasic2DAgent.builder(receiverPopulation)
                .addAction(femaleLikeMating)
                .build();
        female.initialize();
        final Basic2DAgent male = DefaultBasic2DAgent.builder(donorPopulation)
                .addAction(maleLikeMating)
                .build();
        male.initialize();

        final DefaultGreyfishTiled2DSpace space = DefaultGreyfishTiled2DSpaceImpl.ofSize(1, 1);
        final ImmutableSet<Basic2DAgent> prototypes = ImmutableSet.of(male, female);
        final Basic2DSimulation simulation = DefaultBasic2DSimulation.builder(space, prototypes).build();

        simulation.addAgent(male, ImmutablePoint2D.at(0,0));
        simulation.addAgent(female, ImmutablePoint2D.at(0,0));
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
