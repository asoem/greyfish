package org.asoem.greyfish.core.acl;


import org.asoem.greyfish.core.individual.Agent;

public interface ACLMessageReceiver {
	public Iterable<ImmutableACLMessage<Agent>> pollMessages(MessageTemplate p);
	public Iterable<ImmutableACLMessage<Agent>> pollMessages(int i, MessageTemplate p);
}
