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
