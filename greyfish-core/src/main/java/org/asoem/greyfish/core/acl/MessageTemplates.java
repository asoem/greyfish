/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.acl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
public final class MessageTemplates {

    private MessageTemplates() {}

    public static MessageTemplate performative(final ACLPerformative performative) {
        return new PerformativeMessageTemplate(performative);
    }

    public static MessageTemplate ontology(@Nullable final String ontology) {
        return new LiteralTemplate(ontology, ACLLiteralMessageField.ONTOLOGY);
    }

    public static MessageTemplate conversationId(final int conversationId) {
        return new ConversationIdMessageTemplate(conversationId);
    }

    public static MessageTemplate inReplyTo(final String inReplyTo) {
        return new LiteralTemplate(inReplyTo, ACLLiteralMessageField.IN_REPLY_TO);
    }

    public static MessageTemplate replyWith(final String replyWith) {
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

    public static MessageTemplate and(final MessageTemplate t1, final MessageTemplate t2) {
        return forPredicate(Predicates.<ACLMessage<?>>and(t1, t2));
    }

    public static MessageTemplate and(final MessageTemplate ... templates) {
        return forPredicate(Predicates.<ACLMessage<?>>and(templates));
    }

    public static MessageTemplate or(final MessageTemplate t1, final MessageTemplate t2) {
        return forPredicate(Predicates.<ACLMessage<?>>or(t1, t2));
    }

    public static MessageTemplate or(final MessageTemplate... templates) {
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
        public boolean apply(@Nullable final ACLMessage<?> input) {
            return true;
        }
    }

    private enum AlwaysFalseTemplate implements MessageTemplate {
        INSTANCE;

        @Override
        public boolean apply(@Nullable final ACLMessage<?> input) {
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
        public boolean apply(final ACLMessage<?> object) {

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

        public PerformativeMessageTemplate(final ACLPerformative performative) {
            this.performative = performative;
        }

        @Override
        public boolean apply(final ACLMessage<?> aclMessage) {
            return checkNotNull(aclMessage).getPerformative().equals(performative);
        }
    }

    private static class ConversationIdMessageTemplate implements MessageTemplate, Serializable {
        private final int conversationId;

        public ConversationIdMessageTemplate(final int conversationId) {
            this.conversationId = conversationId;
        }

        @Override
        public boolean apply(final ACLMessage<?> aclMessage) {
            return aclMessage.getConversationId() == conversationId;
        }
    }

    private static class ReplyToMessageTemplate implements MessageTemplate, Serializable {
        private final ACLMessage<?> message;

        public ReplyToMessageTemplate(final ACLMessage<?> message) {
            this.message = message;
        }

        @Override
        public boolean apply(final ACLMessage<?> reply) {
            final ACLMessage<?> replyNonNull = checkNotNull(reply);
            return replyNonNull.getConversationId() == message.getConversationId()
                    && message.getReplyWith().equals(replyNonNull.getInReplyTo());
        }
    }

    private static class ContentMessageTemplate<T> implements MessageTemplate, Serializable {
        private final Class<T> clazz;
        private final Predicate<T> predicate;

        public ContentMessageTemplate(final Class<T> clazz, final Predicate<T> predicate) {
            this.clazz = clazz;
            this.predicate = predicate;
        }

        @Override
        public boolean apply(final ACLMessage<?> aclMessage) {
            final ACLMessage<?> message = checkNotNull(aclMessage);
            return clazz.isInstance(message.getContent())
                    && predicate.apply(clazz.cast(message.getContent()));
        }
    }

    private static class SentToMessageTemplate implements MessageTemplate, Serializable {
        private final Object receiver;

        public SentToMessageTemplate(final Object receiver) {
            this.receiver = receiver;
        }

        @Override
        public boolean apply(final ACLMessage<?> aclMessage) {
            return Iterables.any(checkNotNull(aclMessage).getRecipients(), Predicates.equalTo(receiver));
        }
    }

    private static class ForwardingMessageTemplate implements MessageTemplate, Serializable {
        private final Predicate<ACLMessage<?>> predicate;

        public ForwardingMessageTemplate(final Predicate<ACLMessage<?>> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean apply(final ACLMessage<?> aclMessage) {
            return predicate.apply(aclMessage);
        }
    }
}
