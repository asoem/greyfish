package org.asoem.greyfish.core.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLMessageReceiver;
import org.asoem.greyfish.core.acl.ACLMessageTransmitter;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.lang.CircularFifoBuffer;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import com.google.common.base.Preconditions;

public class MessageInterface extends AbstractGFComponent implements GFInterface, ACLMessageTransmitter, ACLMessageReceiver {

	/**
	 * max 32 messages
	 */
	private final Collection<ACLMessage> inBox = new CircularFifoBuffer<ACLMessage>() {
		public void elementReplaced(ACLMessage element) {
			ACLMessage.recycle(element);
		};
	};
	
	public MessageInterface() {
	}

	protected MessageInterface(MessageInterface component,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(component, mapDict);
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new MessageInterface(this, mapDict);
	}

	public Collection<ACLMessage> pollMessages(final MessageTemplate messageTemplate) {
		Preconditions.checkNotNull(messageTemplate);
		final Collection<ACLMessage> ret = new ArrayList<ACLMessage>();
		for (Iterator<ACLMessage> iterator = inBox.iterator(); iterator.hasNext();) {
			final ACLMessage aclMessage = (ACLMessage) iterator.next();
			if (messageTemplate.apply(aclMessage)) {
				ret.add(aclMessage);
				iterator.remove();
			}
		}
		
		return ret;
	}
	
	public synchronized void addMessage(final ACLMessage message) {
		inBox.add(Preconditions.checkNotNull(message).createCopy()); // add a copy for save object recycling
	}

	@Override
	public void deliverMessage(ACLMessage message) {
		Preconditions.checkNotNull(message);
		message.setSender(componentOwner);
		for (Individual individual : message.getAllReceiver()) {
			individual.getInterface(MessageInterface.class).addMessage(message);
			if (GreyfishLogger.isTraceEnabled())
				GreyfishLogger.trace("Sending message:"+message);
		}
	}
}
