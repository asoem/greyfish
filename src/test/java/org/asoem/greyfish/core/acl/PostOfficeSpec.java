package org.asoem.greyfish.core.acl;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class PostOfficeSpec extends Specification<PostOffice> {

    public class AnEmptyPostOffice {
        private final PostOffice postOffice = PostOffice.newInstance();


        public void shouldReturnAnAddedMessage() {
            postOffice.addMessage(ACLMessage.with().performative(ACLPerformative.CFP).source(0).addDestinations(1).build());
            int returnedMessageCount = postOffice.pollMessages(1).size();

            specify(returnedMessageCount, must.equal(1));
        }
    }

    public class AFilledPostOffice {
        private final PostOffice postOffice = PostOffice.newInstance();


        public void shouldBeEmptyAfterPollingAllMessages() {

            for (int i = 0; i < 100; i++) {
                  postOffice.addMessage(ACLMessage.with().performative(ACLPerformative.CFP).source(0).addDestinations(i).build());
            }

            postOffice.pollMessages(MessageTemplate.alwaysTrue()).size();

            specify(postOffice.size(), must.equal(0));
        }
    }
}
