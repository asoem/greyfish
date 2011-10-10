package org.asoem.greyfish.core.acl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 10.10.11
 * Time: 15:39
 */
@SuppressWarnings("unused")
public class MessageTemplates {

    public static MessageTemplate performative(final ACLPerformative performative) {
        return new MessageTemplate() {
            @Override
            public boolean apply(@Nullable ACLMessage aclMessage) {
                return checkNotNull(aclMessage).getPerformative().equals(performative);
            }
        };
    }

    public static MessageTemplate ontology(@Nullable String ontology) {
        return new LiteralTemplate(ontology, ACLLiteralMessageField.ONTOLOGY);
    }

    public static MessageTemplate conversationId(final int conversationId) {
        return new MessageTemplate() {
            @Override
            public boolean apply(ACLMessage aclMessage) {
                return aclMessage.getConversationId() == conversationId;
            }
        };
    }

    public static MessageTemplate inReplyTo(String inReplyTo) {
        return new LiteralTemplate(inReplyTo, ACLLiteralMessageField.IN_REPLY_TO);
    }

    public static MessageTemplate replyWith(String replyWith) {
        return new LiteralTemplate(replyWith, ACLLiteralMessageField.REPLY_WITH);
    }

    public static MessageTemplate isReplyTo(final ACLMessage message) {
        return new MessageTemplate() {
            @Override
            public boolean apply(@Nullable ACLMessage reply) {
                ACLMessage replyNonNull = checkNotNull(reply);
                return replyNonNull.getConversationId() == message.getConversationId()
                        && message.getReplyWith().equals(replyNonNull.getInReplyTo());
            }
        };
    }

    public static <T> MessageTemplate referenceContent(final Class<T> clazz, final Predicate<T> predicate) {
        return new MessageTemplate() {
            @Override
            public boolean apply(@Nullable ACLMessage aclMessage) {
                ACLMessage message = checkNotNull(aclMessage);
                try {
                    if (message.getContentClass().isAssignableFrom(clazz))
                        return predicate.apply(message.getReferenceContent(clazz));
                } catch (NotUnderstoodException e) {
                    assert false;
                }
                return false;
            }
        };
    }

    public static MessageTemplate sentTo(final int receiverId) {
        return new MessageTemplate() {
            @Override
            public boolean apply(@Nullable ACLMessage aclMessage) {
                return checkNotNull(aclMessage).getRecipients().contains(receiverId);
            }
        };
    }

    public static MessageTemplate content(String content) {
        return new LiteralTemplate(content, ACLLiteralMessageField.CONTENT);
    }

    public static MessageTemplate and(MessageTemplate t1, MessageTemplate t2) {
        return forPredicate(Predicates.<ACLMessage>and(t1, t2));
    }

    public static MessageTemplate and(MessageTemplate... templates) {
        return forPredicate(Predicates.<ACLMessage>and(templates));
    }

    public static MessageTemplate or(MessageTemplate t1, MessageTemplate t2) {
        return forPredicate(Predicates.<ACLMessage>or(t1, t2));
    }

    public static MessageTemplate or(MessageTemplate... templates) {
        return forPredicate(Predicates.<ACLMessage>or(templates));
    }

    private static final MessageTemplate ALWAYS_FALSE_TEMPLATE = new MessageTemplate() {
        @Override
        public boolean apply(@Nullable ACLMessage aclMessage) {
            return false;
        }
    };

    private static final MessageTemplate ALWAYS_TRUE_TEMPLATE = new MessageTemplate() {
        @Override
        public boolean apply(@Nullable ACLMessage aclMessage) {
            return true;
        }
    };

    public static MessageTemplate alwaysFalse() {
        return ALWAYS_FALSE_TEMPLATE;
    }

    public static MessageTemplate alwaysTrue() {
        return ALWAYS_TRUE_TEMPLATE;
    }

    private enum ACLLiteralMessageField {
        IN_REPLY_TO,
        REPLY_WITH,
        ONTOLOGY,
        CONTENT,
    }

    private static class LiteralTemplate implements MessageTemplate {
        @Nullable private final String literal;
        private final ACLLiteralMessageField field;

        public LiteralTemplate(@Nullable final String literal, final ACLLiteralMessageField field) {
            this.literal = literal;
            this.field = checkNotNull(field);
        }

        @Override
        public boolean apply(ACLMessage object) {

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
                case CONTENT:
                    compare = object.getStringContent();
                    break;
                default:
                    break;
            }

            return literal == null && compare == null
                    || literal != null && compare != null && literal.equals(compare);
        }
    }

    public static MessageTemplate forPredicate(final Predicate<ACLMessage> predicate) {
        return new MessageTemplate() {
            @Override
            public boolean apply(ACLMessage aclMessage) {
                return predicate.apply(aclMessage);
            }
        };
    }
}
