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

    @Mock ACLMessage message;

    @Test
    public void testAddMessage() throws Exception {
        // given
        given(message.getRecipients()).willReturn(Arrays.asList(1));
        PostOffice postOffice = PostOffice.newInstance();

        // when
        int ret = postOffice.addMessage(message);

        // then
        assertThat(ret).isEqualTo(1);
        assertThat(postOffice).hasSize(1);
    }

    @Test
    public void testPollMessage() throws Exception {
        // given
        given(message.getRecipients()).willReturn(Arrays.asList(1));
        PostOffice postOffice = PostOffice.newInstance();
        postOffice.addMessage(message);

        // when
        List<ACLMessage> polledMessages = postOffice.pollMessages(1);

        // then
        assertThat(polledMessages).containsExactly(message);
        assertThat(postOffice).hasSize(0);
    }
}
