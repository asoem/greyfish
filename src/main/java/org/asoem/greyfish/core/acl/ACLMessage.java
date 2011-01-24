package org.asoem.greyfish.core.acl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import javolution.util.FastList;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.lang.BuilderInterface;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static scala.actors.threadpool.Arrays.asList;

public final class ACLMessage {

    private static int progressiveId;

    private final static Object NULL_CONTENT = new Object();

    private ACLMessage(Builder builder) {
        this.reply_to = ImmutableList.copyOf(builder.reply_to);
        this.source = builder.source;
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

    public enum ContentType {
        NULL,
        STRING,
        BYTE_ARRAY,
        OTHER
    }

    public ContentType getContentType() {
        return contentType;
    }

    private final ACLPerformative performative;
    private final Individual source;
    private final List<Individual> dests;
    private final List<Individual> reply_to;
    private final ContentType contentType;
    private final Object content;
    private final String reply_with;
    private final String in_reply_to;
    private final String encoding;
    private final String language;
    private final String ontology;
    private final String protocol;
    private final int conversation_id;

    public java.io.Serializable getContentObject() throws UnreadableException
    {

        try{
            byte[] data = getByteSequenceContent();
            if (data == null)
                return null;
            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
            java.io.Serializable s = (java.io.Serializable)oin.readObject();
            return s;
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

    public Object getReferenceContent() {
        return content;
    }

    public Collection<Individual> getAllReceiver() {
        return dests;
    }

    public Iterator<Individual> getAllReplyTo() {
        if (reply_to == null) {
            return Iterators.emptyIterator();
        }
        else {
            return reply_to.iterator();
        }
    }

    public Individual getSender() {
        return source;
    }

    public ACLPerformative getPerformative() {
        return performative;
    }

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
    public String getStringContent() {
        switch (contentType) {
            case STRING:
                return new String((StringBuffer)content);
            case BYTE_ARRAY:
                return new String((byte[])content);
            default:
            case OTHER:
                return content.toString();
        }
    }

    public byte[] getByteSequenceContent() {
        switch (contentType) {
            default:
            case STRING:
                return content.toString().getBytes();
            case BYTE_ARRAY:
                return (byte[]) content;
        }
    }

    public String getReplyWith() {
        return reply_with;
    }

    public String getInReplyTo() {
        return in_reply_to;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getLanguage() {
        return language;
    }

    public String getOntology() {
        return ontology;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getConversationId() {
        return conversation_id;
    }

    public void send(final ACLMessageTransmitter transmitter) {
        if (Strings.isNullOrEmpty(reply_with))
            new IllegalStateException("reply_with hasn't been set! Builder should check this.");

        transmitter.deliverMessage(this);
    }

    /**
     * create a new ACLMessage that is a reply to this message.
     * In particular, it sets the following parameters of the new message:
     * receiver, language, ontology, protocol, conversation-id,
     * in-reply-to, reply-with.
     * The programmer needs to set the communicative-act and the StringContent.
     * Of course, if he wishes to do that, he can reset any of the fields.
     * @return the ACLMessage to send as a reply
     */
    public Builder replyFrom(Individual individual) {
        return new Builder()
                .performative(this.performative)
                .addDestinations(this.reply_to)
                .addDestinations(this.source)
                .language(this.language)
                .ontology(this.ontology)
                .protocol(this.protocol)
                .in_reply_to(this.reply_with)
                .reply_with(generateReplyWith(individual))
                .source(individual)
                .encoding(this.encoding)
                .conversation_id(this.conversation_id);
    }

    private static String generateReplyWith(final Individual source) {
        if (source != null)
            return source.getName() + java.lang.System.currentTimeMillis();
        else
            return "X" + java.lang.System.currentTimeMillis();
    }

    public boolean matches(MessageTemplate performative) {
        return performative.apply(this);
    }

    public String toString(){
        StringBuffer str = new StringBuffer("(");
        str.append(getPerformative() + "\n");

        Individual sender = getSender();
        if (sender != null)
            str.append(":sender" + " "+ sender.toString()+"\n");
        Iterator<Individual> it = getAllReceiver().iterator();
        if (it.hasNext()) {
            str.append(":receiver" + " (set ");
            while(it.hasNext())
                str.append(it.next().toString()+" ");
            str.append(")\n");
        }
        it = getAllReplyTo();
        if (it.hasNext()) {
            str.append(":reply-to" + " (set \n");
            while(it.hasNext())
                str.append(it.next().toString()+" ");
            str.append(")\n");
        }
        switch (contentType) {
            case BYTE_ARRAY:
                str.append(":StringContent" + " <BINARY> \n");
                break;
            case STRING:
                String content = getStringContent().trim();
                str.append(":StringContent" + " \"" + content + "\" \n");
                break;
            case NULL:
                str.append(":StringContent" + " <Not set> \n");
                break;
            case OTHER:
                str.append(":StringContent" + " <OBJECT> \n");
                break;
        }

        // Description of Content
        str.append(":encoding " + getEncoding() + "\n");
        str.append(":language " + getLanguage() + "\n");
        str.append(":ontology " + getOntology() + "\n");

        // Control of Conversation
        str.append(":protocol " + getProtocol() + "\n");
        str.append(":conversation-id " + getConversationId() + "\n");
        str.append(":reply-with " + getReplyWith() + "\n");
        str.append(":in-reply-to " + getInReplyTo() + "\n");

        str.append(")");

        return str.toString();
    }

    public Builder createCopy() {
        return new Builder()
                .performative(this.performative)
                .contentType(this.contentType)
                .reply_with(this.reply_with)
                .contentType(this.contentType)
                .in_reply_to(this.in_reply_to)
                .encoding(this.encoding)
                .language(this.language)
                .ontology(this.ontology)
                .protocol(this.protocol)
                .addDestinations(this.dests)
                .addReplyTos(this.reply_to)
                .source(this.source);
    }

    public static Builder with() {
        return new Builder();
    }

    public static class Builder implements BuilderInterface<ACLMessage> {
        private ACLPerformative performative;
        private Individual source;
        private final List<Individual> dests = new FastList<Individual>();
        private final List<Individual> reply_to = new FastList<Individual>();
        private ContentType contentType = ContentType.BYTE_ARRAY;
        private Object content = NULL_CONTENT;
        private String reply_with;
        private String in_reply_to;
        private String encoding;
        private String language;
        private String ontology;
        private String protocol;
        private int conversation_id = 0;

        public Builder performative(ACLPerformative performative) { this.performative = checkNotNull(performative); return this; }
        public Builder source(Individual source) { this.source = checkNotNull(source); return this; }
        public Builder reply_with(String reply_with) { this.reply_with = reply_with; return this; }
        public Builder in_reply_to(String in_reply_to) { this.in_reply_to = in_reply_to; return this; }
        public Builder encoding(String encoding) { this.encoding = checkNotNull(encoding); return this; }
        public Builder language(String language) { this.language = checkNotNull(language); return this; }
        public Builder ontology(String ontology) { this.ontology = checkNotNull(ontology); return this; }
        public Builder protocol(String protocol) { this.protocol = checkNotNull(protocol); return this; }
        public Builder conversation_id(int conversation_id) { this.conversation_id = conversation_id; return this; }
        public Builder addDestinations(Individual ... destinations) { this.dests.addAll(asList(checkNotNull(destinations))); return this; }
        public Builder addDestinations(Iterable<Individual> destinations) { Iterables.addAll(dests, checkNotNull(destinations)); return this; }
        public Builder addReplyTos(Individual ... destinations) { this.reply_to.addAll(asList(checkNotNull(destinations))); return this; }
        public Builder addReplyTos(Iterable<Individual> destinations) { Iterables.addAll(reply_to, checkNotNull(destinations)); return this; }

        private Builder contentType(ContentType type) { this.contentType = checkNotNull(type); return this; }

        public Builder stringContent(String content) {
            this.content = content;
            this.contentType = ACLMessage.ContentType.STRING;
            return this;
        }

        public Builder byteSequenceContent(byte[] content) {
            this.content = content;
            this.contentType = ACLMessage.ContentType.BYTE_ARRAY;
            return this;
        }

        public Builder objectContent(Object content) {
            this.content = content;
            this.contentType = ACLMessage.ContentType.OTHER;
            return this;
        }

        @Override
        public ACLMessage build() {
            checkState(source != null);
            checkState(!dests.isEmpty());
            checkState(conversation_id != 0);
            checkState(!Strings.isNullOrEmpty(reply_with));
            checkState(performative != null);

            if (conversation_id == 0)
                conversation_id = ++progressiveId;

            return new ACLMessage(this);
        }
    }
}
