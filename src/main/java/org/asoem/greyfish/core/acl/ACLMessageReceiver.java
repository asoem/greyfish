package org.asoem.greyfish.core.acl;


public interface ACLMessageReceiver {
	public Iterable<ImmutableACLMessage> pollMessages(MessageTemplate p);
	public Iterable<ImmutableACLMessage> pollMessages(int i, MessageTemplate p);
}
