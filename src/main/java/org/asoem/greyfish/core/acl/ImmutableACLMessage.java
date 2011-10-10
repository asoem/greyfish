package org.asoem.greyfish.core.acl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import javolution.util.FastSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableACLMessage implements ACLMessage {

    private static int progressiveId;

    private final static Object NULL_CONTENT = new Object();

    private ImmutableACLMessage(Builder builder) {
        this.reply_to = ImmutableList.copyOf(builder.reply_to);
        this.sender = builder.sender;
        this.dests = ImmutableList.copyOf(builder.dests);
        this.reply_with = builder.reply_with;
        this.content = builder.content;
        this.contentType = builder.contentType;
        this.conversation_id = builder.conversation_id;
        this.encoding = builder.encoding;
        this.in_reply_to = builder.in_reply_to;
        this.ontology = builder.ontology;
        this.performative = builder.performative;
        this.language = builder.language;
        this.protocol = builder.protocol;
    }

    @Override
    public Class<?> getContentClass() {
        return content == null ? null : content.getClass();
    }

    public static Builder replyTo(ACLMessage message, int id) {
        return new Builder()
                .performative(message.getPerformative())
                .addReplyTos(message.getAllReplyTo())
                .addReceiver(message.getSender())
                .language(message.getLanguage())
                .ontology(message.getOntology())
                .protocol(message.getProtocol())
                .in_reply_to(message.getReplyWith())
                .reply_with(generateReplyWith(id))
                .sender(id)
                .encoding(message.getEncoding())
                .conversation_id(message.getConversationId());
    }

    public enum ContentType {
        NULL,
        STRING,
        BYTE_ARRAY,
        OTHER
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    private final ACLPerformative performative;
    private final int sender;
    private final List<Integer> dests;
    private final List<Integer> reply_to;
    private final ContentType contentType;
    private final Object content;
    private final String reply_with;
    private final String in_reply_to;
    private final String encoding;
    private final String language;
    private final String ontology;
    private final String protocol;
    private final int conversation_id;

    @Override
    public java.io.Serializable getContentObject() throws UnreadableException
    {

        try{
            byte[] data = getByteSequenceContent();
            if (data == null)
                return null;
            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
            return Serializable.class.cast(oin.readObject());
        }
        catch (java.lang.Error e) {
            throw new UnreadableException(e.getMessage());
        }
        catch (IOException e1) {
            throw new UnreadableException(e1.getMessage());
        }
        catch(ClassNotFoundException e2) {
            throw new UnreadableException(e2.getMessage());
        }

    }

    @Override
    public <T> T getReferenceContent(Class<T> clazz) throws NotUnderstoodException {
        if(!checkNotNull(clazz).isInstance(content))
            throw NotUnderstoodException.unexpectedPayloadType(this, clazz);
//            throw new IllegalArgumentException("Requesting " + valueType + " content which has type " + content.getClass());
        return clazz.cast(content);
    }

    @Override
    public List<Integer> getRecipients() {
        return dests;
    }

    @Override
    public List<Integer> getAllReplyTo() {
        return reply_to;
    }

    @Override
    public Integer getSender() {
        return sender;
    }

    @Override
    public ACLPerformative getPerformative() {
        return performative;
    }

    @Override
    public String getStringContent() {
        switch (contentType) {
            case STRING:
                return (String) content;
            case BYTE_ARRAY:
                return new String((byte[])content);
            default:
            case OTHER:
                return content.toString();
        }
    }

    @Override
    public byte[] getByteSequenceContent() {
        switch (contentType) {
            default:
            case STRING:
                return content.toString().getBytes();
            case BYTE_ARRAY:
                return (byte[]) content;
        }
    }

    @Override
    public String getReplyWith() {
        return reply_with;
    }

    @Override
    public String getInReplyTo() {
        return in_reply_to;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public String getOntology() {
        return ontology;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public int getConversationId() {
        return conversation_id;
    }

    @Override
    public void send(final ACLMessageTransmitter transmitter) {
        transmitter.deliverMessage(this);
    }

    /**
     * create a new ACLMessage that is a reply to this message.
     * In particular, it sets the following parameters of the new message:
     * receiver, language, ontology, protocol, conversation-id,
     * in-reply-to, reply-with.
     * The programmer needs to set the communicative-act and the StringContent.
     * Of course, if he wishes to do that, he can reset any of the fields.
     * @param sender the sender
     * @return the ACLMessage to send as a reply
     */
    public Builder createReplyFrom(int sender) {
        return replyTo(this, sender);
    }

    private static String generateReplyWith(final int source) {
        return String.valueOf(source) + java.lang.System.currentTimeMillis();
    }

    @Override
    public boolean matches(MessageTemplate performative) {
        return performative.apply(this);
    }

    public String toString(){
        final StringBuffer str = new StringBuffer("(");

        str.append(getPerformative()).append("\n");
        str.append(":sender" + " ").append(sender).append("\n");

        str.append(":receiver [" + Joiner.on(" ").join(dests) + "]\n");
        str.append(":reply-to [" + Joiner.on(" ").join(reply_to) + "]\n");

        switch (contentType) {
            case BYTE_ARRAY:
                str.append(":StringContent" + " <BINARY> \n");
                break;
            case STRING:
                str.append(":StringContent" + " \"").append(getStringContent().trim()).append("\" \n");
                break;
            case NULL:
                str.append(":StringContent" + " <Not set> \n");
                break;
            case OTHER:
                str.append(":StringContent" + " <").append(this.content.getClass().getSimpleName()).append("> \n");
                break;
        }

        // Description of Content
        str.append(":encoding ").append(getEncoding()).append("\n");
        str.append(":language ").append(getLanguage()).append("\n");
        str.append(":ontology ").append(getOntology()).append("\n");

        // Control of Conversation
        str.append(":protocol ").append(getProtocol()).append("\n");
        str.append(":conversation-id ").append(getConversationId()).append("\n");
        str.append(":reply-with ").append(getReplyWith()).append("\n");
        str.append(":in-reply-to ").append(getInReplyTo()).append("\n");

        str.append(")");

        return str.toString();
    }

    public static Builder with() {
        return new Builder();
    }

    public static class Builder implements org.asoem.greyfish.lang.Builder<ImmutableACLMessage> {
        private ACLPerformative performative;
        private int sender = -1;
        private final Set<Integer> dests = FastSet.newInstance();
        private final Set<Integer> reply_to = FastSet.newInstance();
        private ContentType contentType = ContentType.NULL;
        private Object content = NULL_CONTENT;
        private String reply_with;
        private String in_reply_to;
        private String encoding;
        private String language;
        private String ontology;
        private String protocol;
        private int conversation_id = 0;

        public Builder performative(ACLPerformative performative) { this.performative = checkNotNull(performative); return this; }
        public Builder sender(int source) { this.sender = source; return this; }
        public Builder reply_with(String reply_with) { this.reply_with = reply_with; return this; }
        public Builder in_reply_to(String in_reply_to) { this.in_reply_to = in_reply_to; return this; }

        public Builder encoding(String encoding) { this.encoding = encoding; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder ontology(String ontology) { this.ontology = ontology; return this; }
        public Builder protocol(String protocol) { this.protocol = protocol; return this; }

        public Builder conversation_id(int conversation_id) { this.conversation_id = conversation_id; return this; }

        public Builder addReceiver(int destinations) { this.dests.add(checkNotNull(destinations)); return this; }
        public Builder addReceiver(int... destinations) { this.dests.addAll(Ints.asList(checkNotNull(destinations))); return this; }
        public Builder addReceivers(Iterable<Integer> destinations) { Iterables.addAll(dests, checkNotNull(destinations)); return this; }

        public Builder addReplyTos(int destinations) { this.reply_to.add(checkNotNull(destinations)); return this; }
        public Builder addReplyTos(int ... destinations) { this.reply_to.addAll(Ints.asList(checkNotNull(destinations))); return this; }
        public Builder addReplyTos(Iterable<Integer> destinations) { Iterables.addAll(reply_to, checkNotNull(destinations)); return this; }

        private Builder contentType(ContentType type) { this.contentType = checkNotNull(type); return this; }

        public Builder stringContent(String content) {
            this.content = content;
            this.contentType = ImmutableACLMessage.ContentType.STRING;
            return this;
        }

        public Builder byteSequenceContent(byte[] content) {
            this.content = content;
            this.contentType = ImmutableACLMessage.ContentType.BYTE_ARRAY;
            return this;
        }

        public <T> Builder objectContent(T content) {
            this.content = content;
            this.contentType = ImmutableACLMessage.ContentType.OTHER;
            return this;
        }

        @Override
        public ImmutableACLMessage build() {
            if (sender <= -1)           throw new IllegalStateException("Invalid sender id: " + sender);
            if (dests.isEmpty())        throw new  IllegalStateException("No receiver defined");
            if (conversation_id < 0)   throw new IllegalStateException("Invalid conversation ID: " + conversation_id);
            if (conversation_id == 0)   conversation_id = ++progressiveId;
            if (reply_with == null)     reply_with = generateReplyWith(sender);
            if (Strings.isNullOrEmpty(reply_with)) throw new IllegalStateException("Invalid reply_with string: '" + reply_with + "'");
            if (performative == null)   throw new IllegalStateException("No performative defined");

            return new ImmutableACLMessage(this);
        }
    }
}
