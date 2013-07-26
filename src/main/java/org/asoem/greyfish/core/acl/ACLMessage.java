package org.asoem.greyfish.core.acl;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * User: christoph
 * Date: 10.10.11
 * Time: 13:56
 */
public interface ACLMessage<T> {

    Set<T> getRecipients();

    Set<T> getAllReplyTo();

    /**
     * It is possible to omit the sender parameter if, for example, the agent sending the ACL message wishes to remain anonymous.
     * @return the sender of the message
     */
    @Nullable
    T getSender();

    ACLPerformative getPerformative();

    String getReplyWith();

    @Nullable
    String getInReplyTo();

    @Nullable
    String getEncoding();

    @Nullable
    String getLanguage();

    @Nullable
    String getOntology();

    @Nullable
    String getProtocol();

    int getConversationId();

    void send(ACLMessageTransmitter transmitter);

    boolean matches(MessageTemplate performative);

    <C> C userDefinedParameter(String key, Class<C> clazz);

    @Nullable
    Object getContent();
}
