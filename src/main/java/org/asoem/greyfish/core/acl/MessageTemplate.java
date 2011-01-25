package org.asoem.greyfish.core.acl;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import static com.google.common.base.Preconditions.checkNotNull;

public class MessageTemplate implements Predicate<ACLMessage> {


    private static class PerformativeTemplate implements Predicate<ACLMessage> {

		private final ACLPerformative performative;
		
		public PerformativeTemplate(final ACLPerformative performative) {
			this.performative = checkNotNull(performative);
		}
		
		@Override
		public boolean apply(final ACLMessage object) {
			return performative.equals(object.getPerformative());
		}	
	}
	
	private enum ACLLiteralMessageField {
		IN_REPLY_TO,
		REPLY_WITH,
		ONTOLOGY,
		CONTENT,
	}
	
	private static class LiteralTemplate implements Predicate<ACLMessage> {
		private final String literal;
		private final ACLLiteralMessageField field;
		
		public LiteralTemplate(final String literal, final ACLLiteralMessageField field) {
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

    private static class ConversationIDTemplate implements Predicate<ACLMessage> {
        private int conversationID;

        private ConversationIDTemplate(int conversationID) {
            this.conversationID = conversationID;
        }

        @Override
        public boolean apply(ACLMessage aclMessage) {
            return this.conversationID == aclMessage.getConversationId();
        }
    }


    private static class ReferenceContentTemplate<T> implements Predicate<ACLMessage> {
        private Class<T> clazz;
        private Predicate<T> predicate;

        private ReferenceContentTemplate(Class<T> clazz, Predicate<T> predicate) {
            this.clazz = clazz;
            this.predicate = predicate;
        }

        @Override
        public boolean apply(ACLMessage aclMessage) {
            return predicate.apply(aclMessage.getReferenceContent(clazz));
        }
    }

	private static class IsReplyTemplate implements Predicate<ACLMessage> {
		private ACLMessage messages;
		
		public IsReplyTemplate(ACLMessage messages) {
			this.messages = checkNotNull(messages);
		}

		@Override
		public boolean apply(ACLMessage object) {
			return Objects.equal(messages.getConversationId(), object.getConversationId())
                    && messages.getReplyWith().equals(object.getInReplyTo());
		}
	}

    private Predicate<ACLMessage> predicate;

	private MessageTemplate(Predicate<ACLMessage> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean apply(ACLMessage object) {
		return predicate.apply(checkNotNull(object));
	}
	
	public static MessageTemplate performative(ACLPerformative performative) {
		return new MessageTemplate(new PerformativeTemplate(performative));
	}
	
	public static MessageTemplate ontology(String ontology) {
		return new MessageTemplate(new LiteralTemplate(ontology, ACLLiteralMessageField.ONTOLOGY));
	}

    public static MessageTemplate conversationId(int conversationId) {
        return new MessageTemplate(new ConversationIDTemplate(conversationId));
    }

	public static MessageTemplate all(MessageTemplate ... templates) {
		return new MessageTemplate(Predicates.<ACLMessage>and(templates));
	}

	public static MessageTemplate or(MessageTemplate t1, MessageTemplate t2) {
		return new MessageTemplate(Predicates.<ACLMessage>or(t1, t2));
	}

	public static MessageTemplate inReplyTo(String inReplyTo) {
		return new MessageTemplate(new LiteralTemplate(inReplyTo, ACLLiteralMessageField.IN_REPLY_TO));
	}

	public static MessageTemplate and(MessageTemplate t1, MessageTemplate t2) {
		return new MessageTemplate(Predicates.<ACLMessage>and(t1, t2));
	}

	public static MessageTemplate replyWith(String replyWith) {
		return new MessageTemplate(new LiteralTemplate(replyWith, ACLLiteralMessageField.REPLY_WITH));
	}

	public static MessageTemplate isReplyTo(ACLMessage cfpReplies) {
		return new MessageTemplate(new IsReplyTemplate(cfpReplies));
	}

	public static MessageTemplate content(String content) {
		return new MessageTemplate(new LiteralTemplate(content, ACLLiteralMessageField.CONTENT));
	}

	public static MessageTemplate any(MessageTemplate ... templates) {
		return new MessageTemplate(Predicates.<ACLMessage>or(templates));
	}

    private static final MessageTemplate ALWAYS_FALSE_TEMPLATE = new MessageTemplate(Predicates.<ACLMessage>alwaysFalse());
    public static MessageTemplate alwaysFalse() {
        return ALWAYS_FALSE_TEMPLATE;
    }

    private static final MessageTemplate ALWAYS_TRUE_TEMPLATE = new MessageTemplate(Predicates.<ACLMessage>alwaysFalse());
    public static MessageTemplate alwaysTrue() {
        return ALWAYS_TRUE_TEMPLATE;
    }
    public static <T> MessageTemplate referenceContent(Class<T> clazz, Predicate<T> predicate) {
        return new MessageTemplate(new ReferenceContentTemplate(clazz, predicate));
    }

}
