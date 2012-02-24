package org.asoem.greyfish.core.individual;

import com.google.inject.Inject;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import java.awt.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 16:25
 */
public class AvatarTest {

    @Inject
    private Persister persister;

    public AvatarTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
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
