package org.asoem.greyfish.core.acl;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class PostOfficeSpec extends Specification<PostOffice> {

    public class AnEmptyPostOffice {
        private PostOffice postOffice = PostOffice.newInstance();


        public void shouldReturnAnAddedMessage() {
            postOffice.addMessage(ACLMessage.with().performative(ACLPerformative.CFP).source(0).addDestinations(1).build());
            int returnedMessageCount = postOffice.getMessages(1, MessageTemplate.alwaysTrue()).size();

            specify(returnedMessageCount, must.equal(1));
        }
    }
}
