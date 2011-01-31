package org.asoem.greyfish.core.interfaces;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLMessageReceiver;
import org.asoem.greyfish.core.acl.ACLMessageTransmitter;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.CircularFifoBuffer;
import org.asoem.greyfish.utils.CloneMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class MessageInterface extends AbstractGFComponent implements GFInterface, ACLMessageTransmitter, ACLMessageReceiver {

	/**
	 * max 32 messages
	 */
	private final Collection<ACLMessage> inBox = new CircularFifoBuffer<ACLMessage>();

    private Simulation simulation;
	
	private MessageInterface(Builder builder) {
        super(builder);
	}

    public MessageInterface(MessageInterface messageInterface, CloneMap map) {
        super(messageInterface, map);
    }

    public static MessageInterface newInstance() {
        return new Builder().build();
    }

    public static class Builder extends AbstractBuilder<Builder> implements BuilderInterface<MessageInterface> {
        @Override
        protected Builder self() {
            return this;
        }

        public MessageInterface build() { return new MessageInterface(this); }
    }

    @Override
    public MessageInterface deepCloneHelper(CloneMap map) {
        return new MessageInterface(this, map);
    }

    public Collection<ACLMessage> pollMessages(final MessageTemplate messageTemplate) {
		Preconditions.checkNotNull(messageTemplate);
		this.inBox.addAll(simulation.getPostOffice().getMessages(componentOwner.getId()));

        final Collection<ACLMessage> ret = new ArrayList<ACLMessage>();
		for (Iterator<ACLMessage> iterator = inBox.iterator(); iterator.hasNext();) {
			final ACLMessage aclMessage = iterator.next();
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
//		for (Integer individual : message.getAllReceiver()) {
//			individual.getInterface(MessageInterface.class).addMessage(message);
//			if (GreyfishLogger.isTraceEnabled())
//				GreyfishLogger.trace("Sending message:"+message);
//		}
        simulation.getPostOffice().addMessage(message);
	}

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        this.simulation = simulation;
    }
}
