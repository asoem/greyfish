package org.asoem.greyfish.core.acl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableACLMessage<T extends AgentIdentifier> implements ACLMessage<T> {

    private static int progressiveId;

    private final static Object NULL_CONTENT = new Object();
    private final Map<String, Object> userDefinedParameter;

    private ImmutableACLMessage(Builder<T> builder) {
        this.reply_to = ImmutableSet.copyOf(builder.reply_to);
        this.sender = builder.sender;
        this.receiver = ImmutableSet.copyOf(builder.dests);
        this.reply_with = builder.reply_with;
        this.content = builder.content;
        this.contentType = builder.contentType;
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
    public Class<?> getContentClass() {
        return contentType;
    }

    public static <T extends AgentIdentifier> Builder<T> createReply(ACLMessage<T> message, T id) {
        checkNotNull(message);
        checkArgument(message.getSender() != null, "Cannot reply to an anonymous sender");
        return new Builder<T>()
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
                .conversationId(message.getConversationId());
    }

    private final ACLPerformative performative;

    private final T sender;
    private final Set<T> receiver;
    private final Set<T> reply_to;

    private final Object content;
    private final Class<?> contentType;

    private final String language;
    private final String encoding;
    private final String ontology;

    private final String protocol;
    private final int conversationId;
    private final String reply_with;
    private final String inReplyTo;

    @Override
    public <C> C getContent(Class<C> clazz) {
        return clazz.cast(content);
    }

    @Override
    public Set<T> getRecipients() {
        return receiver;
    }

    @Override
    public Set<T> getAllReplyTo() {
        return reply_to;
    }

    @Override
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
    public String getInReplyTo() {
        return inReplyTo;
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
        return conversationId;
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
    public Builder createReplyFrom(T sender) {
        return createReply(this, sender);
    }

    private static String generateReplyWith(final AgentIdentifier source) {
        return String.valueOf(source) + java.lang.System.currentTimeMillis();
    }

    @Override
    public boolean matches(MessageTemplate performative) {
        return performative.apply(this);
    }

    @Override
    public <C> C userDefinedParameter(String key, Class<C> clazz) {
        return clazz.cast(userDefinedParameter.get(key));
    }

    public String toString(){
        final StringBuilder str = new StringBuilder("(");

        str.append(getPerformative()).append("\n");
        str.append(":sender" + " ").append(sender).append("\n");

        str.append(":receiver [").append(Joiner.on(" ").join(receiver)).append("]\n");
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

    public static <T extends AgentIdentifier> Builder<T> with() {
        return new Builder<T>();
    }

    public static class Builder<T extends AgentIdentifier> implements org.asoem.greyfish.utils.base.Builder<ImmutableACLMessage<T>> {
        private ACLPerformative performative;
        private T sender;
        private final Set<T> dests = Sets.newHashSet();
        private final Set<T> reply_to = Sets.newHashSet();
        private Class<?> contentType;
        private Object content = NULL_CONTENT;
        private String reply_with;
        private String inReplyTo;
        private String encoding;
        private String language;
        private String ontology;
        private String protocol;
        private int conversationId = 0;
        private Map<String, Object> userDefinedParameter = Maps.newHashMap();

        public Builder<T> performative(ACLPerformative performative) { this.performative = checkNotNull(performative); return this; }
        public Builder<T> sender(T source) { this.sender = source; return this; }
        public Builder<T> reply_with(String reply_with) { this.reply_with = reply_with; return this; }
        public Builder<T> in_reply_to(String in_reply_to) { this.inReplyTo = in_reply_to; return this; }

        public Builder<T> encoding(String encoding) { this.encoding = encoding; return this; }
        public Builder<T> language(String language) { this.language = language; return this; }
        public Builder<T> ontology(String ontology) { this.ontology = ontology; return this; }
        public Builder<T> protocol(String protocol) { this.protocol = protocol; return this; }

        public Builder<T> conversationId(int conversationId) { this.conversationId = conversationId; return this; }

        public Builder<T> addReceiver(T destinations) { this.dests.add(checkNotNull(destinations)); return this; }
        public Builder<T> addReceiver(T ... destinations) { this.dests.addAll(Arrays.asList(checkNotNull(destinations))); return this; }
        public Builder<T> addReceivers(Iterable<? extends T> destinations) { Iterables.addAll(dests, checkNotNull(destinations)); return this; }

        public Builder<T> addReplyTos(T destinations) { this.reply_to.add(checkNotNull(destinations)); return this; }
        public Builder<T> addReplyTos(T ... destinations) { this.reply_to.addAll(Arrays.asList(checkNotNull(destinations))); return this; }
        public Builder<T> addReplyTos(Iterable<? extends T> destinations) { Iterables.addAll(reply_to, checkNotNull(destinations)); return this; }

        public <C> Builder<T> content(C content, Class<C> contentType) {
            this.content = content;
            this.contentType = contentType;
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
            /* FIPA says: "It is only permissible to omit the receiver parameter if the message recipient can be reliably inferred from context" */
            if (dests.isEmpty())        throw new  IllegalStateException("No receiver defined");

            /* FIPA says: An agent MAY tag ACL messages with a conversation identifier */
            if (conversationId < 0)   throw new IllegalStateException("Invalid conversation ID: " + conversationId);
            if (conversationId == 0)   conversationId = ++progressiveId;

            if (reply_with == null)     reply_with = generateReplyWith(sender);
            if (Strings.isNullOrEmpty(reply_with)) throw new IllegalStateException("Invalid reply_with string: '" + reply_with + "'");

            if (performative == null)   throw new IllegalStateException("No performative defined");

            return new ImmutableACLMessage<T>(this);
        }
    }
}
