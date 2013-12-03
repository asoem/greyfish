package org.asoem.greyfish.core.acl;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;

public interface MessageConsumer<A> {
    void receive(ACLMessage<A> message);

    void receiveAll(Iterable<? extends ACLMessage<A>> message);

    Iterable<ACLMessage<A>> getMessages(MessageTemplate template);

    boolean hasMessages(MessageTemplate template);
}
