package org.asoem.greyfish.core.agent;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.space.Point2D;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 16:25
 */
public class AvatarTest {

    @Inject
    private Persister persister;

    public AvatarTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testDeepClone() throws Exception {
        // given
        final SpatialAgent<Basic2DAgent, Basic2DSimulation, Point2D> agentDelegate = mock(SpatialAgent.class);
        given(agentDelegate.deepClone(any(DeepCloner.class))).willReturn(agentDelegate);

        final Avatar<Basic2DAgent, Basic2DSimulation, Point2D> avatar =
                new Avatar<Basic2DAgent, Basic2DSimulation, Point2D>(agentDelegate, mock(Point2D.class));

        // when
        final Avatar<Basic2DAgent, Basic2DSimulation, Point2D> clone = CycleCloner.clone(avatar);

        // then
        assertThat(clone, is(equalTo(avatar)));
    }

    @Test
    public void testPersistence() throws Exception {
        /*
        // given
        final PrototypeGroup population = PrototypeGroup.newPopulation("Test", Color.green);
        final Agent agent = FrozenAgent.of(population).build();
        final Avatar avatar = new Avatar(agent);

        // when
        final Avatar copy = Persisters.createCopy(avatar, Avatar.class, persister);

        // then
        assertThat(copy).isEqualTo(avatar);
        */
    }
}
