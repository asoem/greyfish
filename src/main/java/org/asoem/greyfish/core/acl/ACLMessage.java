package org.asoem.greyfish.core.acl;

import java.util.List;

/**
 * User: christoph
 * Date: 10.10.11
 * Time: 13:56
 */
public interface ACLMessage {
    Class<?> getContentClass();

    ImmutableACLMessage.ContentType getContentType();

    java.io.Serializable getContentObject() throws UnreadableException;

    /**
     *
     * @param clazz The expected type of the content object
     * @param <T> The class type
     * @return The content object casted to type <code>T</code>
     * @throws IllegalArgumentException if content has not type <code>T</code>
     */
    <T> T getReferenceContent(Class<T> clazz) throws NotUnderstoodException;

    List<Integer> getRecipients();

    List<Integer> getAllReplyTo();

    Integer getSender();

    ACLPerformative getPerformative();

    /**
     * Reads <code>:StringContent</code> slot. <p>
     * <p>Notice that, in general, setting a String StringContent and getting
     * back a byte sequence StringContent - or viceversa - does not return
     * the same to, i.e. the following relation does not hold
     * <code>
     * getByteSequenceContent(setByteSequenceContent(getStringContent().getBytes()))
     * is equal to getByteSequenceContent()
     * </code>
     * @return The to of <code>:StringContent</code> slot. Guarantied to be not <code>null</code>
     */
    String getStringContent();

    byte[] getByteSequenceContent();

    String getReplyWith();

    String getInReplyTo();

    String getEncoding();

    String getLanguage();

    String getOntology();

    String getProtocol();

    int getConversationId();

    void send(ACLMessageTransmitter transmitter);

    boolean matches(MessageTemplate performative);
}
