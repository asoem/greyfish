package org.asoem.greyfish.core.individual;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.asoem.greyfish.utils.space.MotionObject2D;
import org.junit.Test;

import java.awt.*;

import static org.fest.assertions.Assertions.assertThat;
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
        final Agent agent = mock(Agent.class);
        given(agent.deepClone(any(DeepCloner.class))).willReturn(agent);
        final Avatar avatar = new Avatar(agent);
        avatar.setProjection(mock(MotionObject2D.class));

        // when
        final Avatar copy = DeepCloner.clone(avatar, Avatar.class);

        // then
        assertThat(copy).isEqualTo(avatar);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final Agent agent = ImmutableAgent.of(population).build();
        final Avatar avatar = new Avatar(agent);

        // when
        final Avatar copy = Persisters.createCopy(avatar, Avatar.class, persister);

        // then
        assertThat(copy).isEqualTo(avatar);
    }
}
