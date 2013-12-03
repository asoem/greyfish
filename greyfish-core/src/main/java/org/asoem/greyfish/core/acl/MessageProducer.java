package org.asoem.greyfish.core.acl;

public interface MessageProducer<A> {
    void sendMessage(ACLMessage<A> message);
}
