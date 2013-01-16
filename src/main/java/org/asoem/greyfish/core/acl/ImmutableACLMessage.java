package org.asoem.greyfish.core.acl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableACLMessage<T> implements ACLMessage<T>, Serializable {

    private static int progressiveId;

    private final Map<String, Object> userDefinedParameter;

    private final ACLPerformative performative;

    @Nullable
    private final T sender;
    private final Set<T> receivers;
    private final Set<T> reply_to;

    private final Object content;

    @Nullable
    private final String language;
    @Nullable
    private final String encoding;
    @Nullable
    private final String ontology;

    @Nullable
    private final String protocol;
    private final int conversationId;
    @Nullable
    private final String reply_with;
    @Nullable
    private final String inReplyTo;

    private ImmutableACLMessage(Builder<T> builder) {
        this.reply_to = builder.reply_to;
        this.sender = builder.sender;
        this.receivers = builder.receivers;
        this.reply_with = builder.reply_with;
        this.content = builder.content;
        this.conversationId = builder.conversationId;
        this.encoding = builder.encoding;
        this.inReplyTo = builder.inReplyTo;
        this.ontology = builder.ontology;
        this.performative = builder.performative;
        this.language = builder.language;
        this.protocol = builder.protocol;
        this.userDefinedParameter = ImmutableMap.copyOf(builder.userDefinedParameter);
    }

    @Override
    public Set<T> getRecipients() {
        return receivers;
    }

    @Override
    public Set<T> getAllReplyTo() {
        return reply_to;
    }

    @Override
    @Nullable
    public T getSender() {
        return sender;
    }

    @Override
    public ACLPerformative getPerformative() {
        return performative;
    }

    @Override
    public String getReplyWith() {
        return reply_with;
    }

    @Override
    @Nullable
    public String getInReplyTo() {
        return inReplyTo;
    }

    @Override
    @Nullable
    public String getEncoding() {
        return encoding;
    }

    @Override
    @Nullable
    public String getLanguage() {
        return language;
    }

    @Override
    @Nullable
    public String getOntology() {
        return ontology;
    }

    @Override
    @Nullable
    public String getProtocol() {
        return protocol;
    }

    @Override
    public int getConversationId() {
        return conversationId;
    }

    @Override
    public void send(final ACLMessageTransmitter transmitter) {
        transmitter.deliverMessage(this);
    }

    /**
     * create a new ACLMessage that is a reply to this message.
     * In particular, it sets the following parameters of the new message:
     * receivers, language, ontology, protocol, conversation-id,
     * in-reply-to, reply-with.
     * The programmer needs to set the communicative-act and the StringContent.
     * Of course, if he wishes to do that, he can reset any of the fields.
     * @param sender the sender
     * @return the ACLMessage to send as a reply
     */
    @SuppressWarnings("UnusedDeclaration")
    public Builder<T> replyTo(T sender) {
        return createReply(this, sender);
    }

    @Override
    public boolean matches(MessageTemplate performative) {
        return performative.apply(this);
    }

    @Override
    public <C> C userDefinedParameter(String key, Class<C> clazz) {
        return clazz.cast(userDefinedParameter.get(key));
    }

    @Override
    @Nullable
    public Object getContent() {
        return content;
    }

    public String toString(){
        final StringBuilder str = new StringBuilder("(");

        str.append(getPerformative()).append("\n");
        str.append(":sender" + " ").append(sender).append("\n");

        str.append(":receivers [").append(Joiner.on(" ").join(receivers)).append("]\n");
        str.append(":reply-to [").append(Joiner.on(" ").join(reply_to)).append("]\n");

        str.append(":Content" + " <").append(this.content.getClass().getSimpleName()).append("> \n");

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableACLMessage)) return false;

        ImmutableACLMessage that = (ImmutableACLMessage) o;

        if (conversationId != that.conversationId) return false;
        if (content != null ? !content.equals(that.content) : that.content != null) return false;
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) return false;
        if (inReplyTo != null ? !inReplyTo.equals(that.inReplyTo) : that.inReplyTo != null) return false;
        if (language != null ? !language.equals(that.language) : that.language != null) return false;
        if (ontology != null ? !ontology.equals(that.ontology) : that.ontology != null) return false;
        if (performative != that.performative) return false;
        if (protocol != null ? !protocol.equals(that.protocol) : that.protocol != null) return false;
        if (!receivers.equals(that.receivers)) return false;
        if (!reply_to.equals(that.reply_to)) return false;
        if (reply_with != null ? !reply_with.equals(that.reply_with) : that.reply_with != null) return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        if (!userDefinedParameter.equals(that.userDefinedParameter)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userDefinedParameter.hashCode();
        result = 31 * result + performative.hashCode();
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + receivers.hashCode();
        result = 31 * result + reply_to.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        result = 31 * result + (ontology != null ? ontology.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + conversationId;
        result = 31 * result + (reply_with != null ? reply_with.hashCode() : 0);
        result = 31 * result + (inReplyTo != null ? inReplyTo.hashCode() : 0);
        return result;
    }

    private static String generateReplyWith(final Object source) {
        return String.valueOf(source) + java.lang.System.currentTimeMillis();
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static <T> Builder<T> createReply(ACLMessage<T> message, T id) {
        checkNotNull(message);
        checkArgument(message.getSender() != null, "Cannot reply to an anonymous sender");
        return new Builder<T>()
                .performative(message.getPerformative())
                .addReplyTo(message.getAllReplyTo())
                .addReceiver(message.getSender())
                .language(message.getLanguage())
                .ontology(message.getOntology())
                .protocol(message.getProtocol())
                .in_reply_to(message.getReplyWith())
                .reply_with(generateReplyWith(id))
                .sender(id)
                .encoding(message.getEncoding())
                .conversationId(message.getConversationId());
    }

    private Object writeReplace() {
        return new Builder<T>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Builder<T> implements org.asoem.greyfish.utils.base.Builder<ImmutableACLMessage<T>>, Serializable {
        private ACLPerformative performative;
        private T sender;
        private final Set<T> receivers = Sets.newHashSet();
        private final Set<T> reply_to = Sets.newHashSet();
        private Object content;
        private String reply_with;
        private String inReplyTo;
        private String encoding;
        private String language;
        private String ontology;
        private String protocol;
        private int conversationId = 0;
        private final Map<String, Object> userDefinedParameter = Maps.newHashMap();

        public Builder(ImmutableACLMessage<T> message) {
            this.performative = message.performative;
            this.sender = message.sender;
            this.receivers.addAll(message.receivers);
            this.reply_to.addAll(message.reply_to);
            this.content = message.content;
            this.reply_with = message.reply_with;
            this.inReplyTo = message.inReplyTo;
            this.encoding = message.encoding;
            this.language = message.language;
            this.ontology = message.ontology;
            this.protocol = message.protocol;
            this.conversationId = message.conversationId;
            this.userDefinedParameter.putAll(message.userDefinedParameter);
        }

        public Builder() {
        }

        public Builder<T> performative(ACLPerformative performative) { this.performative = checkNotNull(performative); return this; }
        public Builder<T> sender(T source) { this.sender = source; return this; }
        public Builder<T> reply_with(String reply_with) { this.reply_with = reply_with; return this; }
        public Builder<T> in_reply_to(String in_reply_to) { this.inReplyTo = in_reply_to; return this; }

        public Builder<T> encoding(String encoding) { this.encoding = encoding; return this; }
        public Builder<T> language(String language) { this.language = language; return this; }
        public Builder<T> ontology(String ontology) { this.ontology = ontology; return this; }
        public Builder<T> protocol(String protocol) { this.protocol = protocol; return this; }

        public Builder<T> conversationId(int conversationId) { this.conversationId = conversationId; return this; }

        public Builder<T> addReceiver(T receiver) { this.receivers.add(receiver); return this; }
        public Builder<T> setReceivers(T ... receivers) { this.receivers.addAll(Arrays.asList(receivers)); return this; }

        public Builder<T> addReplyTo(T replyTo) { this.reply_to.add(replyTo); return this; }
        public Builder<T> addReplyTo(T... replyToReceivers) { this.reply_to.addAll(Arrays.asList(replyToReceivers)); return this; }
        public Builder<T> addReplyTo(Set<T> replyToReceivers) { this.reply_to.addAll(replyToReceivers); return this; }

        public <C> Builder<T> content(C content, Class<C> contentType) {
            this.content = checkNotNull(content);
            return this;
        }

        public Builder<T> addUserDefinedParameter(String name, Object value) {
            checkNotNull(name);
            checkArgument(!userDefinedParameter.containsKey(name), "No duplicate parameters allowed");
            checkNotNull(value);
            userDefinedParameter.put(name, value);
            return this;
        }

        @Override
        public ImmutableACLMessage<T> build() {
            /* FIPA says: "It is only permissible to omit the receivers parameter if the message recipient can be reliably inferred from context" */
            if (receivers.isEmpty())        throw new  IllegalStateException("No receivers defined");

            /* FIPA says: An agent MAY tag ACL messages with a conversation identifier */
            if (conversationId < 0)   throw new IllegalStateException("Invalid conversation ID: " + conversationId);
            if (conversationId == 0)   conversationId = ++progressiveId; // TODO: Use a message factory instead which supplies an id generator

            if (reply_with == null)     reply_with = generateReplyWith(sender);
            if (Strings.isNullOrEmpty(reply_with)) throw new IllegalStateException("Invalid reply_with string: '" + reply_with + "'");

            if (performative == null)   throw new IllegalStateException("No performative defined");

            return new ImmutableACLMessage<T>(this);
        }

        private Object readResolve() {
            return build();
        }

        private static final long serialVersionUID = 0;
    }
}
