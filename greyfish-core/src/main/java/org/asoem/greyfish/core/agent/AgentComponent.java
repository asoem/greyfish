package org.asoem.greyfish.core.agent;

import java.io.Serializable;

public interface AgentComponent<C> extends AgentNode, Serializable {

    /**
     * @return the name of this component
     */
    String getName();

    <T> T ask(C context, Object message, Class<T> replyType);
}
