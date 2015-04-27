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

package org.asoem.greyfish.core.agent;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;


public class AvatarTest {

    @Inject
    private Persister persister;

    public AvatarTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    /*
    @Test
    public void testDeepClone() throws Exception {
        // given
        final SpatialAgent<Basic2DAgent, Point2D, BasicSimulationContext<Basic2DSimulation, Basic2DAgent>> agentDelegate = mock(SpatialAgent.class);
        given(agentDelegate.deepClone(any(DeepCloner.class))).willReturn(agentDelegate);

        final Avatar<Basic2DAgent, Basic2DSimulation, Point2D, BasicSimulationContext<Basic2DSimulation, Basic2DAgent>> avatar =
                new Avatar<Basic2DAgent, Basic2DSimulation, Point2D, BasicSimulationContext<Basic2DSimulation, Basic2DAgent>>(agentDelegate, mock(Point2D.class));

        // when
        final Avatar<Basic2DAgent, Basic2DSimulation, Point2D, BasicSimulationContext<Basic2DSimulation, Basic2DAgent>> clone = CycleCloner.clone(avatar);

        // then
        assertThat(clone, is(equalTo(avatar)));
    }
    */

    /*
    @Test
    public void testPersistence() throws Exception {

        // given
        final PrototypeGroup population = PrototypeGroup.newPopulation("Test", Color.green);
        final Agent agent = FrozenAgent.of(population).build();
        final Avatar avatar = new Avatar(agent);

        // when
        final Avatar copy = Persisters.createCopy(avatar, Avatar.class, persister);

        // then
        assertThat(copy).isEqualTo(avatar);

    }
    */
}
