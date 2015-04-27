/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.agent_interaction;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import org.asoem.greyfish.core.actions.FemaleLikeMating;
import org.asoem.greyfish.core.actions.MaleLikeMating;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.DefaultBasic2DAgent;
import org.asoem.greyfish.impl.environment.Basic2DEnvironment;
import org.asoem.greyfish.impl.environment.DefaultBasic2DEnvironment;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.impl.space.DefaultBasicTiled2DSpace;
import org.asoem.greyfish.utils.base.Callbacks;
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
                DefaultBasic2DAgent agent = DefaultBasic2DAgent.builder()
                        .addAction(femaleLikeMating).build();
                agent.initialize();
                return agent;
            }
        };

        final Supplier<DefaultBasic2DAgent> maleFactory = new Supplier<DefaultBasic2DAgent>() {
            @Override
            public DefaultBasic2DAgent get() {
                DefaultBasic2DAgent agent = DefaultBasic2DAgent.builder()
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
