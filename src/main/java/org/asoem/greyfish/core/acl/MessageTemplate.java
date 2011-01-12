package org.asoem.greyfish.core.acl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

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
	
	private static class IsReplyTemplate implements Predicate<ACLMessage> {
		private Collection<ACLMessage> messages;
		
		public IsReplyTemplate(Collection<ACLMessage> messages) {
			this.messages = checkNotNull(messages);
		}

		@Override
		public boolean apply(ACLMessage object) {
			return Iterables.any(messages, MessageTemplate.replyWith(object.getInReplyTo()));
		}
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
	
	private Predicate<ACLMessage> predicate;
	
	private MessageTemplate(Predicate<ACLMessage> predicate) {
		this.predicate = predicate;
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

	public static MessageTemplate isReply(Collection<ACLMessage> cfpReplies) {
		return new MessageTemplate(new IsReplyTemplate(cfpReplies));
	}

	public static MessageTemplate content(String content) {
		return new MessageTemplate(new LiteralTemplate(content, ACLLiteralMessageField.CONTENT));
	}

	public static MessageTemplate any(MessageTemplate ... templates) {
		return new MessageTemplate(Predicates.<ACLMessage>or(templates));
	}
}
