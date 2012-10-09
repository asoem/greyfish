package org.asoem.greyfish.core.agent;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.space.MotionObject2D;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        final DeepCloner clonerMock = mock(DeepCloner.class);
        final Agent cloneMock = mock(Agent.class);
        given(clonerMock.getClone(any(Agent.class), any(Class.class))).willReturn(cloneMock);
        final Agent agent = mock(Agent.class);
        final Avatar avatar = new Avatar(agent);
        avatar.setProjection(mock(MotionObject2D.class));

        // when
        final Avatar clone = avatar.deepClone(clonerMock);

        // then
        verify(clonerMock).addClone(eq(avatar), any(Avatar.class));
        verify(clonerMock).getClone(agent, Agent.class);
        assertThat(clone.delegate()).isEqualTo(cloneMock);
    }

    @Test
    public void testPersistence() throws Exception {
        /*
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final Agent agent = ImmutableAgent.of(population).build();
        final Avatar avatar = new Avatar(agent);

        // when
        final Avatar copy = Persisters.createCopy(avatar, Avatar.class, persister);

        // then
        assertThat(copy).isEqualTo(avatar);
        */
    }
}
