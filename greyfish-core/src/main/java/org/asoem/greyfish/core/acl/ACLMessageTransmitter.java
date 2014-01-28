package org.asoem.greyfish.core.acl;

public interface ACLMessageTransmitter {

    void deliverMessage(final ImmutableACLMessage<?> message);
}
