package org.asoem.greyfish.core.acl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ImmutableACLMessage<T> implements ACLMessage<T>, Serializable {

    private static int progressiveId;

    private final Map<String, Object> userDefinedParameter;

    private final ACLPerformative performative;

    @Nullable
    private final T sender;
    private final Set<T> receivers;
    private final Set<T> replyTo;

    @Nullable
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
    private final String replyWith;

    @Nullable
    private final String inReplyTo;

    private ImmutableACLMessage(final Builder<T> builder) {
        this.replyTo = builder.replyTo;
        this.sender = builder.sender;
        this.receivers = builder.receivers;
        this.replyWith = builder.replyWith;
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
        return replyTo;
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

    @Nullable
    @Override
    public String getReplyWith() {
        return replyWith;
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
     * create a new ACLMessage that is a reply to this message. In particular, it sets the following parameters of the
     * new message: receivers, language, ontology, protocol, conversation-id, in-reply-to, reply-with. The programmer
     * needs to set the communicative-act and the StringContent. Of course, if he wishes to do that, he can reset any of
     * the fields.
     *
     * @param sender the sender
     * @return the ACLMessage to send as a reply
     */
    @SuppressWarnings("UnusedDeclaration")
    public Builder<T> replyTo(final T sender) {
        return createReply(this, sender);
    }

    @Override
    public boolean matches(final MessageTemplate performative) {
        return performative.apply(this);
    }

    @Override
    public <C> C userDefinedParameter(final String key, final Class<C> clazz) {
        return clazz.cast(userDefinedParameter.get(key));
    }

    @Override
    @Nullable
    public Object getContent() {
        return content;
    }

    public String toString() {
        final StringBuilder str = new StringBuilder("(");

        str.append(getPerformative()).append("\n");
        str.append(":sender" + " ").append(sender).append("\n");

        str.append(":receivers [").append(Joiner.on(" ").join(receivers)).append("]\n");
        str.append(":reply-to [").append(Joiner.on(" ").join(replyTo)).append("]\n");

        str.append(":Content" + " <").append(this.content).append("> \n");

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

    @SuppressWarnings({"rawtypes", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableACLMessage)) {
            return false;
        }

        final ImmutableACLMessage that = (ImmutableACLMessage) o;

        if (conversationId != that.conversationId) {
            return false;
        }
        if (content != null ? !content.equals(that.content) : that.content != null) {
            return false;
        }
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) {
            return false;
        }
        if (inReplyTo != null ? !inReplyTo.equals(that.inReplyTo) : that.inReplyTo != null) {
            return false;
        }
        if (language != null ? !language.equals(that.language) : that.language != null) {
            return false;
        }
        if (ontology != null ? !ontology.equals(that.ontology) : that.ontology != null) {
            return false;
        }
        if (performative != that.performative) {
            return false;
        }
        if (protocol != null ? !protocol.equals(that.protocol) : that.protocol != null) {
            return false;
        }
        if (!receivers.equals(that.receivers)) {
            return false;
        }
        if (!replyTo.equals(that.replyTo)) {
            return false;
        }
        if (replyWith != null ? !replyWith.equals(that.replyWith) : that.replyWith != null) {
            return false;
        }
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) {
            return false;
        }
        if (!userDefinedParameter.equals(that.userDefinedParameter)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = userDefinedParameter.hashCode();
        result = 31 * result + performative.hashCode();
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + receivers.hashCode();
        result = 31 * result + replyTo.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        result = 31 * result + (ontology != null ? ontology.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + conversationId;
        result = 31 * result + (replyWith != null ? replyWith.hashCode() : 0);
        result = 31 * result + (inReplyTo != null ? inReplyTo.hashCode() : 0);
        return result;
    }

    private static String generateReplyWith(final Object source) {
        return String.valueOf(source) + System.currentTimeMillis();
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static <T> Builder<T> createReply(final ACLMessage<T> message, final T id) {
        checkNotNull(message);
        checkArgument(message.getSender() != null, "Cannot reply to an anonymous sender");
        return new Builder<T>()
                .performative(message.getPerformative())
                .addReplyTo(message.getAllReplyTo())
                .addReceiver(message.getSender())
                .language(message.getLanguage())
                .ontology(message.getOntology())
                .protocol(message.getProtocol())
                .inReplyTo(message.getReplyWith())
                .replyWith(generateReplyWith(id))
                .sender(id)
                .encoding(message.getEncoding())
                .conversationId(message.getConversationId());
    }

    private Object writeReplace() {
        return new Builder<T>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final class Builder<T> implements org.asoem.greyfish.utils.base.Builder<ImmutableACLMessage<T>>, Serializable {
        @Nullable
        private ACLPerformative performative;
        @Nullable
        private T sender;
        private final Set<T> receivers = Sets.newHashSet();
        private final Set<T> replyTo = Sets.newHashSet();
        @Nullable
        private Object content;
        @Nullable
        private String replyWith;
        @Nullable
        private String inReplyTo;
        @Nullable
        private String encoding;
        @Nullable
        private String language;
        @Nullable
        private String ontology;
        @Nullable
        private String protocol;
        private int conversationId = 0;
        private final Map<String, Object> userDefinedParameter = Maps.newHashMap();

        public Builder(final ImmutableACLMessage<T> message) {
            this.performative = message.performative;
            this.sender = message.sender;
            this.receivers.addAll(message.receivers);
            this.replyTo.addAll(message.replyTo);
            this.content = message.content;
            this.replyWith = message.replyWith;
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

        public Builder<T> performative(final ACLPerformative performative) {
            this.performative = checkNotNull(performative);
            return this;
        }

        public Builder<T> sender(final T source) {
            this.sender = source;
            return this;
        }

        public Builder<T> replyWith(final String reply_with) {
            this.replyWith = reply_with;
            return this;
        }

        public Builder<T> inReplyTo(final String in_reply_to) {
            this.inReplyTo = in_reply_to;
            return this;
        }

        public Builder<T> encoding(final String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder<T> language(final String language) {
            this.language = language;
            return this;
        }

        public Builder<T> ontology(final String ontology) {
            this.ontology = ontology;
            return this;
        }

        public Builder<T> protocol(final String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder<T> conversationId(final int conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder<T> addReceiver(final T receiver) {
            this.receivers.add(receiver);
            return this;
        }

        public Builder<T> setReceivers(final T... receivers) {
            Collections.addAll(this.receivers, receivers);
            return this;
        }

        public Builder<T> addReplyTo(final T replyTo) {
            this.replyTo.add(replyTo);
            return this;
        }

        public Builder<T> addReplyTo(final T... replyToReceivers) {
            Collections.addAll(this.replyTo, replyToReceivers);
            return this;
        }

        public Builder<T> addReplyTo(final Set<T> replyToReceivers) {
            this.replyTo.addAll(replyToReceivers);
            return this;
        }

        public <C> Builder<T> content(final C content) {
            this.content = checkNotNull(content, "Message content must not be null");
            return this;
        }

        public Builder<T> addUserDefinedParameter(final String name, final Object value) {
            checkNotNull(name);
            checkArgument(!userDefinedParameter.containsKey(name), "No duplicate parameters allowed");
            checkNotNull(value);
            userDefinedParameter.put(name, value);
            return this;
        }

        @Override
        public ImmutableACLMessage<T> build() {
            /* FIPA says: "It is only permissible to omit the receivers parameter if the message recipient can be reliably inferred from context" */
            if (receivers.isEmpty()) {
                throw new IllegalStateException("No receivers defined");
            }

            /* FIPA says: An agent MAY tag ACL messages with a conversation identifier */
            if (conversationId < 0) {
                throw new IllegalStateException("Invalid conversation ID: " + conversationId);
            }
            if (conversationId == 0) {
                conversationId = ++progressiveId; // TODO: Use a message factory instead which supplies an id generator
            }

            if (replyWith == null) {
                replyWith = generateReplyWith(sender);
            }
            if (Strings.isNullOrEmpty(replyWith)) {
                throw new IllegalStateException("Invalid reply_with string: '" + replyWith + "'");
            }

            if (performative == null) {
                throw new IllegalStateException("No performative defined");
            }

            return new ImmutableACLMessage<T>(this);
        }

        private Object readResolve() {
            return build();
        }

        private static final long serialVersionUID = 0;
    }
}
