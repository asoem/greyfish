package org.asoem.greyfish.core.acl;

public interface MessageConsumer<A> {
    void receive(ACLMessage<A> message);

    Iterable<ACLMessage<A>> getMessages(MessageTemplate template);

}
