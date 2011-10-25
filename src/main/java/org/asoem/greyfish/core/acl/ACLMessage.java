package org.asoem.greyfish.core.acl;

import java.util.Set;

/**
 * User: christoph
 * Date: 10.10.11
 * Time: 13:56
 */
public interface ACLMessage<T extends AgentIdentifier> {
    Class<?> getContentClass();
    /**
     *
     * @param clazz The expected type of the content object
     * @param <C> The class type
     * @return The content object casted to type {@code C}
     * @throws ClassCastException if content can not be cast to type {@code C}
     */
    <C> C getContent(Class<C> clazz) throws ClassCastException;

    Set<T> getRecipients();

    Set<T> getAllReplyTo();

    /**
     * It is possible to omit the sender parameter if, for example, the agent sending the ACL message wishes to remain anonymous.
     * @return the sender of the message
     */
    T getSender(); // TODO: Change to Optional<AgentIdentifier>

    ACLPerformative getPerformative();

    String getReplyWith();

    String getInReplyTo();

    String getEncoding();

    String getLanguage();

    String getOntology();

    String getProtocol();

    int getConversationId();

    void send(ACLMessageTransmitter transmitter);

    boolean matches(MessageTemplate performative);

    <C> C userDefinedParameter(String key, Class<C> clazz);
}
