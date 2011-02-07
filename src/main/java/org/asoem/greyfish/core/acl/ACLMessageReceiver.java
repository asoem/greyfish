package org.asoem.greyfish.core.acl;


public interface ACLMessageReceiver {
	public Iterable<ACLMessage> pollMessages(MessageTemplate p);
	public Iterable<ACLMessage> pollMessages(int i, MessageTemplate p);
}
