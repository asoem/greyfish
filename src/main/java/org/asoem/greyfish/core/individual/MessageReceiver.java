package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.acl.ACLMessage;

/**
 * User: christoph
 * Date: 18.02.11
 * Time: 15:43
 */
public interface MessageReceiver {
    void receiveMessage(ACLMessage message);
    void receiveMessages(Iterable<? extends ACLMessage> message);
    int getId();
}
