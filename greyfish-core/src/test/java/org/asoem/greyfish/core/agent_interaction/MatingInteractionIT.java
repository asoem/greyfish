package org.asoem.greyfish.core.agent_interaction;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import org.asoem.greyfish.core.actions.FemaleLikeMating;
import org.asoem.greyfish.core.actions.MaleLikeMating;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.DefaultBasic2DAgent;
import org.asoem.greyfish.impl.simulation.Basic2DEnvironment;
import org.asoem.greyfish.impl.simulation.DefaultBasic2DEnvironment;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.impl.space.DefaultBasicTiled2DSpace;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.collect.LoadingKeyedObjectPool;
import org.asoem.greyfish.utils.collect.SynchronizedKeyedObjectPool;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.SimpleTwoDimTreeFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(MockitoJUnitRunner.class)
public class MatingInteractionIT {

    public MatingInteractionIT() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testNormalInteraction() throws Exception {
        // given
        final PrototypeGroup receiverPrototypeGroup = PrototypeGroup.named("receiverPopulation");
        final PrototypeGroup donorPrototypeGroup = PrototypeGroup.named("donorPopulation");

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


        final Supplier<DefaultBasic2DAgent> femaleFactory = new Supplier<DefaultBasic2DAgent>() {
            @Override
            public DefaultBasic2DAgent get() {
                DefaultBasic2DAgent agent = DefaultBasic2DAgent.builder(receiverPrototypeGroup)
                        .addAction(femaleLikeMating).build();
                agent.initialize();
                return agent;
            }
        };

        final Supplier<DefaultBasic2DAgent> maleFactory = new Supplier<DefaultBasic2DAgent>() {
            @Override
            public DefaultBasic2DAgent get() {
                DefaultBasic2DAgent agent = DefaultBasic2DAgent.builder(donorPrototypeGroup)
                        .addAction(maleLikeMating).build();
                agent.initialize();
                return agent;
            }
        };

        final Basic2DAgent female = femaleFactory.get();
        final Basic2DAgent male = maleFactory.get();

        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, SimpleTwoDimTreeFactory.<Basic2DAgent>newInstance());
        final ImmutableSet<Basic2DAgent> prototypes = ImmutableSet.of(male, female);
        final Basic2DEnvironment simulation = DefaultBasic2DEnvironment.builder(space, prototypes)
                .agentPool(SynchronizedKeyedObjectPool.<PrototypeGroup, Basic2DAgent>create(new LoadingKeyedObjectPool.PoolLoader<PrototypeGroup, Basic2DAgent>() {
                    @Override
                    public Basic2DAgent load(final PrototypeGroup prototypeGroup) {
                        if (receiverPrototypeGroup.equals(prototypeGroup)) {
                            return femaleFactory.get();
                        } else if (donorPrototypeGroup.equals(prototypeGroup)) {
                            return maleFactory.get();
                        } else {
                            throw new AssertionError();
                        }
                    }
                }))
                .build();

        simulation.enqueueAddition(male, ImmutablePoint2D.at(0, 0));
        simulation.enqueueAddition(female, ImmutablePoint2D.at(0, 0));
        for (int i1 = 0; i1 < 6; i1++) {
            simulation.nextStep();
        }

        // then
        assertThat(femaleLikeMating.getReceivedSperm(), hasSize(1));
    }
}
