package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * User: christoph
 * Date: 15.01.13
 * Time: 17:29
 */
public class FixedSizeMessageBoxTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final FixedSizeMessageBox<DefaultGreyfishAgent> messageBox = new FixedSizeMessageBox<DefaultGreyfishAgent>();
        messageBox.add(mock(AgentMessage.class, withSettings().serializable()));
        messageBox.add(mock(AgentMessage.class, withSettings().serializable()));

        // when
        final FixedSizeMessageBox<DefaultGreyfishAgent> copy = Persisters.createCopy(messageBox, Persisters.javaSerialization());

        // then
        assertThat(copy, Matchers.hasSize(messageBox.size()));
    }
}
