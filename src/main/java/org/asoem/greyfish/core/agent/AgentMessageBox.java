package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.acl.MessageTemplate;

import java.util.List;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 14:37
 */
public interface AgentMessageBox extends Iterable<AgentMessage> {
    void push(AgentMessage message);

    Iterable<AgentMessage> filter(MessageTemplate template);

    void clear();

    void pushAll(Iterable<? extends AgentMessage> message);

    List<AgentMessage> consume(MessageTemplate template);

    List<AgentMessage> messages();
}
