package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.acl.MessageConsumer;
import org.asoem.greyfish.core.acl.MessageProducer;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.PrototypeGroup;

public interface AgentContext<A extends Agent<?>> extends MessageProducer<A>, MessageConsumer<A> {
    A agent();

    Iterable<A> getActiveAgents();

    Iterable<A> getAgents(PrototypeGroup prototypeGroup);
}
