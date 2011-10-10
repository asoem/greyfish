package org.asoem.greyfish.core.acl;

import java.io.Serializable;
import java.util.List;

/**
 * User: christoph
 * Date: 10.10.11
 * Time: 13:57
 */
public abstract class ForwardingACLMessage implements ACLMessage {

    protected abstract ACLMessage delegate();
    
    @Override
    public Class<?> getContentClass() {
        return delegate().getContentClass();
    }

    @Override
    public ImmutableACLMessage.ContentType getContentType() {
        return delegate().getContentType();
    }

    @Override
    public Serializable getContentObject() throws UnreadableException {
        return delegate().getContentObject();
    }

    @Override
    public <T> T getReferenceContent(Class<T> clazz) throws NotUnderstoodException {
        return delegate().getReferenceContent(clazz);
    }

    @Override
    public List<Integer> getRecipients() {
        return delegate().getRecipients();
    }

    @Override
    public List<Integer> getAllReplyTo() {
        return delegate().getAllReplyTo();
    }

    @Override
    public Integer getSender() {
        return delegate().getSender();
    }

    @Override
    public ACLPerformative getPerformative() {
        return delegate().getPerformative();
    }

    @Override
    public String getStringContent() {
        return delegate().getStringContent();
    }

    @Override
    public byte[] getByteSequenceContent() {
        return delegate().getByteSequenceContent();
    }

    @Override
    public String getReplyWith() {
        return delegate().getReplyWith();
    }

    @Override
    public String getInReplyTo() {
        return delegate().getInReplyTo();
    }

    @Override
    public String getEncoding() {
        return delegate().getEncoding();
    }

    @Override
    public String getLanguage() {
        return delegate().getLanguage();
    }

    @Override
    public String getOntology() {
        return delegate().getOntology();
    }

    @Override
    public String getProtocol() {
        return delegate().getProtocol();
    }

    @Override
    public int getConversationId() {
        return delegate().getConversationId();
    }

    @Override
    public void send(ACLMessageTransmitter transmitter) {
        delegate().send(transmitter);
    }

    @Override
    public boolean matches(MessageTemplate performative) {
        return delegate().matches(performative);
    }
}
