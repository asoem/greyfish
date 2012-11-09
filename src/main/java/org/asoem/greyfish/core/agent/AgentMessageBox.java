package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.acl.MessageTemplate;

import java.util.List;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 14:37
 */
public interface AgentMessageBox<A extends Agent> extends Iterable<AgentMessage<A>> {
    void push(AgentMessage<A> message);

    Iterable<AgentMessage<A>> filter(MessageTemplate template);

    void clear();

    void pushAll(Iterable<? extends AgentMessage<A>> message);

    List<AgentMessage<A>> consume(MessageTemplate template);

    List<AgentMessage<A>> messages();
}
