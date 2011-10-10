package org.asoem.greyfish.core.acl;

public interface ACLMessageTransmitter {

	public void deliverMessage(final ImmutableACLMessage message);
}
