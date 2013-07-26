package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ForwardingACLMessage;

import java.io.Serializable;

/**
 * User: christoph
 * Date: 15.10.11
 * Time: 15:42
 */
public class AgentMessage<A extends Agent<A, ?>> extends ForwardingACLMessage<A> implements Serializable {

    private final ACLMessage<A> delegate;
    private final int receivedTimestamp;

    public AgentMessage(final ACLMessage<A> delegate, final int receivedTimestamp) {
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AgentMessage that = (AgentMessage) o;

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
