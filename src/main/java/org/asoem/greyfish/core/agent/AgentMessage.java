package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ForwardingACLMessage;

/**
 * User: christoph
 * Date: 15.10.11
 * Time: 15:42
 */
public class AgentMessage<A extends Agent<A, ?>> extends ForwardingACLMessage<A> {
    private final ACLMessage<A> delegate;

    private final int receivedTimestamp;

    public AgentMessage(ACLMessage<A> delegate, int receivedTimestamp) {
        this.delegate = delegate;
        this.receivedTimestamp = receivedTimestamp;
    }

    @Override
    protected ACLMessage<A> delegate() {
       return delegate;
    }

    public int getReceivedTimestamp() {
        return receivedTimestamp;
    }

    @Override
    public String toString() {
        return "AgentMessage{" +
                "delegate=" + delegate +
                ", receivedTimestamp=" + receivedTimestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentMessage that = (AgentMessage) o;

        if (receivedTimestamp != that.receivedTimestamp) return false;
        if (!delegate.equals(that.delegate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + receivedTimestamp;
        return result;
    }
}
