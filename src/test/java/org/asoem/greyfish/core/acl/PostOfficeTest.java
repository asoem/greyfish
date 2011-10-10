package org.asoem.greyfish.core.acl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class PostOfficeTest {

    @Mock
    ImmutableACLMessage message;

    @Test
    public void testDispatch() throws Exception {
        // given
        given(message.getRecipients()).willReturn(Arrays.asList(1));
        PostOffice postOffice = PostOffice.newInstance();

        // when
        int messagesToDeliver = postOffice.dispatch(message);

        // then
        assertThat(messagesToDeliver).isEqualTo(1);
        assertThat(postOffice).hasSize(1);
    }

    @Test
    public void testPollMessages() throws Exception {
        // given
        given(message.getRecipients()).willReturn(Arrays.asList(1));
        PostOffice postOffice = PostOffice.newInstance();
        postOffice.dispatch(message);

        // when
        List<ACLMessage> polledMessages = postOffice.pollMessages(1);

        // then
        assertThat(polledMessages).containsExactly(message);
        assertThat(postOffice).hasSize(0);
    }
}
