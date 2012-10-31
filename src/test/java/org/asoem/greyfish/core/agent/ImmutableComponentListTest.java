package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;


/**
 * User: christoph
 * Date: 10.10.12
 * Time: 19:38
 */
public class ImmutableComponentListTest {

    @Test
    public void testSerialization() throws Exception {
        // given
        ImmutableComponentList<AgentComponent> list = ImmutableComponentList.of();

        // when
        final ImmutableComponentList copy = Persisters.createCopy(list, JavaPersister.INSTANCE);

        // then
        assertThat(copy, hasSize(list.size()));
    }
}
