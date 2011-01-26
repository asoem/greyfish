package org.asoem.greyfish.core.interfaces;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLMessageReceiver;
import org.asoem.greyfish.core.acl.ACLMessageTransmitter;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.lang.CircularFifoBuffer;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class MessageInterface extends AbstractGFComponent implements GFInterface, ACLMessageTransmitter, ACLMessageReceiver {

	/**
	 * max 32 messages
	 */
	private final Collection<ACLMessage> inBox = new CircularFifoBuffer<ACLMessage>();
	
	private MessageInterface(Builder builder) {
        super(builder);
	}

    public static MessageInterface newInstance() {
        return new Builder().build();
    }

    public static class Builder extends AbstractBuilder<Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        protected Builder fromClone(MessageInterface component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            return super.fromClone(component, mapDict);
        }

        public MessageInterface build() { return new MessageInterface(this); }
    }

	@Override
	protected AbstractGFComponent deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
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
		for (Individual individual : message.getAllReceiver()) {
			individual.getInterface(MessageInterface.class).addMessage(message);
			if (GreyfishLogger.isTraceEnabled())
				GreyfishLogger.trace("Sending message:"+message);
		}
	}
}
