package org.asoem.sico.core.acl;

public interface ACLMessageTransmitter {

	public void deliverMessage(final ACLMessage message);
}
