package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.FixedSizeMessageBox;
import org.asoem.greyfish.core.acl.ImmutableACLMessage;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 15.01.13
 * Time: 17:29
 */
public class FixedSizeMessageBoxTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final FixedSizeMessageBox<ACLMessage<Integer>> messageBox = FixedSizeMessageBox.withCapacity(1);
        final ImmutableACLMessage<Integer> message = ImmutableACLMessage.<Integer>builder()
                .addReceiver(0)
                .performative(ACLPerformative.ACCEPT_PROPOSAL)
                .build();
        messageBox.add(message);

        // when
        final FixedSizeMessageBox<ACLMessage<Integer>> copy = Persisters.copyAsync(messageBox, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(messageBox)));
    }
}
