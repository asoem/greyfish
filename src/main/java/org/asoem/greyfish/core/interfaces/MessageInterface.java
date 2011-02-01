package org.asoem.greyfish.core.interfaces;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLMessageReceiver;
import org.asoem.greyfish.core.acl.ACLMessageTransmitter;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.CircularFifoBuffer;
import org.asoem.greyfish.utils.CloneMap;

import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MessageInterface extends AbstractGFComponent implements GFInterface, ACLMessageTransmitter, ACLMessageReceiver {

	/**
	 * max 32 messages
	 */
	private final Collection<ACLMessage> inBox = CircularFifoBuffer.newInstance();

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
		checkNotNull(messageTemplate);
        for (ACLMessage message : simulation.getPostOffice().getMessages(componentOwner.getId()))
            addMessage(message);

        final Collection<ACLMessage> ret = Lists.newArrayList();
		for (Iterator<ACLMessage> iterator = inBox.iterator(); iterator.hasNext();) {
			final ACLMessage aclMessage = iterator.next();
			if (messageTemplate.apply(aclMessage)) {
				ret.add(aclMessage);
				iterator.remove();
			}
		}
		
		return ret;
	}
	
	private synchronized void addMessage(final ACLMessage message) {
		inBox.add(checkNotNull(message).createCopy()); // add a copy for save object recycling
	}

	@Override
	public void deliverMessage(ACLMessage message) {
		checkNotNull(message);
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
