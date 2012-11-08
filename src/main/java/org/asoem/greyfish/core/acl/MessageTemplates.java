package org.asoem.greyfish.core.acl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 10.10.11
 * Time: 15:39
 */
@SuppressWarnings("unused")
public final class MessageTemplates {

    private MessageTemplates() {}

    public static MessageTemplate performative(final ACLPerformative performative) {
        return new PerformativeMessageTemplate(performative);
    }

    public static MessageTemplate ontology(@Nullable String ontology) {
        return new LiteralTemplate(ontology, ACLLiteralMessageField.ONTOLOGY);
    }

    public static MessageTemplate conversationId(final int conversationId) {
        return new ConversationIdMessageTemplate(conversationId);
    }

    public static MessageTemplate inReplyTo(String inReplyTo) {
        return new LiteralTemplate(inReplyTo, ACLLiteralMessageField.IN_REPLY_TO);
    }

    public static MessageTemplate replyWith(String replyWith) {
        return new LiteralTemplate(replyWith, ACLLiteralMessageField.REPLY_WITH);
    }

    public static MessageTemplate isReplyTo(final ACLMessage<?> message) {
        return new ReplyToMessageTemplate(message);
    }

    public static <T> MessageTemplate content(final Class<T> clazz, final Predicate<T> predicate) {
        return new ContentMessageTemplate<T>(clazz, predicate);
    }

    public static MessageTemplate sentTo(final Object receiver) {
        return new SentToMessageTemplate(receiver);
    }

    public static MessageTemplate and(MessageTemplate t1, MessageTemplate t2) {
        return forPredicate(Predicates.<ACLMessage<?>>and(t1, t2));
    }

    public static MessageTemplate and(MessageTemplate ... templates) {
        return forPredicate(Predicates.<ACLMessage<?>>and(templates));
    }

    public static MessageTemplate or(MessageTemplate t1, MessageTemplate t2) {
        return forPredicate(Predicates.<ACLMessage<?>>or(t1, t2));
    }

    public static MessageTemplate or(MessageTemplate... templates) {
        return forPredicate(Predicates.<ACLMessage<?>>or(templates));
    }

    public static MessageTemplate alwaysFalse() {
        return AlwaysFalseTemplate.INSTANCE;
    }

    public static MessageTemplate alwaysTrue() {
        return AlwaysTrueTemplate.INSTANCE;
    }

    private enum ACLLiteralMessageField {
        IN_REPLY_TO,
        REPLY_WITH,
        ONTOLOGY,
    }

    private enum AlwaysTrueTemplate implements MessageTemplate {
        INSTANCE;

        @Override
        public boolean apply(@Nullable ACLMessage<?> input) {
            return true;
        }
    }

    private enum AlwaysFalseTemplate implements MessageTemplate {
        INSTANCE;

        @Override
        public boolean apply(@Nullable ACLMessage<?> input) {
            return false;
        }
    }

    private static class LiteralTemplate implements MessageTemplate, Serializable {
        @Nullable private final String literal;
        private final ACLLiteralMessageField field;

        public LiteralTemplate(@Nullable final String literal, final ACLLiteralMessageField field) {
            this.literal = literal;
            this.field = checkNotNull(field);
        }

        @Override
        public boolean apply(ACLMessage<?> object) {

            String compare = null;
            switch (field) {
                case IN_REPLY_TO:
                    compare = object.getInReplyTo();
                    break;
                case ONTOLOGY:
                    compare = object.getOntology();
                    break;
                case REPLY_WITH:
                    compare = object.getReplyWith();
                    break;
                default:
                    break;
            }

            return literal == null && compare == null
                    || literal != null && compare != null && literal.equals(compare);
        }

        private static final long serialVersionUID = 0;
    }

    public static MessageTemplate forPredicate(final Predicate<ACLMessage<?>> predicate){
        return new ForwardingMessageTemplate(predicate);
    }

    private static class PerformativeMessageTemplate implements MessageTemplate, Serializable {
        private final ACLPerformative performative;

        public PerformativeMessageTemplate(ACLPerformative performative) {
            this.performative = performative;
        }

        @Override
        public boolean apply(ACLMessage<?> aclMessage) {
            return checkNotNull(aclMessage).getPerformative().equals(performative);
        }
    }

    private static class ConversationIdMessageTemplate implements MessageTemplate, Serializable {
        private final int conversationId;

        public ConversationIdMessageTemplate(int conversationId) {
            this.conversationId = conversationId;
        }

        @Override
        public boolean apply(ACLMessage<?> aclMessage) {
            return aclMessage.getConversationId() == conversationId;
        }
    }

    private static class ReplyToMessageTemplate implements MessageTemplate, Serializable {
        private final ACLMessage<?> message;

        public ReplyToMessageTemplate(ACLMessage<?> message) {
            this.message = message;
        }

        @Override
        public boolean apply(ACLMessage<?> reply) {
            ACLMessage<?> replyNonNull = checkNotNull(reply);
            return replyNonNull.getConversationId() == message.getConversationId()
                    && message.getReplyWith().equals(replyNonNull.getInReplyTo());
        }
    }

    private static class ContentMessageTemplate<T> implements MessageTemplate, Serializable {
        private final Class<T> clazz;
        private final Predicate<T> predicate;

        public ContentMessageTemplate(Class<T> clazz, Predicate<T> predicate) {
            this.clazz = clazz;
            this.predicate = predicate;
        }

        @Override
        public boolean apply(ACLMessage<?> aclMessage) {
            ACLMessage<?> message = checkNotNull(aclMessage);
            return message.getContentClass().isAssignableFrom(clazz)
                    && predicate.apply(message.getContent(clazz));
        }
    }

    private static class SentToMessageTemplate implements MessageTemplate, Serializable {
        private final Object receiver;

        public SentToMessageTemplate(Object receiver) {
            this.receiver = receiver;
        }

        @Override
        public boolean apply(ACLMessage<?> aclMessage) {
            return Iterables.any(checkNotNull(aclMessage).getRecipients(), Predicates.equalTo(receiver));
        }
    }

    private static class ForwardingMessageTemplate implements MessageTemplate, Serializable {
        private final Predicate<ACLMessage<?>> predicate;

        public ForwardingMessageTemplate(Predicate<ACLMessage<?>> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean apply(ACLMessage<?> aclMessage) {
            return predicate.apply(aclMessage);
        }
    }
}
