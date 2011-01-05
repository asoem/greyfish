package org.asoem.sico.core.acl;


public interface ACLMessageReceiver {
	public Iterable<ACLMessage> pollMessages(MessageTemplate p);
}
