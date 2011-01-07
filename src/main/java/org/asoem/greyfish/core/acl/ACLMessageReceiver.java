package org.asoem.greyfish.core.acl;


public interface ACLMessageReceiver {
	public Iterable<ACLMessage> pollMessages(MessageTemplate p);
}
