package org.asoem.greyfish.core.actions;

import javolution.util.FastList;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.interfaces.MessageInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Collection;
import java.util.Map;

public abstract class ContractNetResponderAction extends FSMAction {

	private static final String CHECK_CFP = "Check-cfp";
	private static final String WAIT_FOR_ACCEPT = "Wait-for-accept";
	private static final String END = "End";
	private static final String TIMEOUT = "Timeout";

	private static final int TIMEOUT_TIME = 10;
	
	private int timeoutCounter;

    private ACLMessage sent;

	private int nExpectedProposeAnswers;
	private Collection<ACLMessage> cfpReplies = new FastList<ACLMessage>();

	public ContractNetResponderAction() {
		initFSM();
	}

	public ContractNetResponderAction(String name) {
		super(name);
		initFSM();
	}

	public ContractNetResponderAction(ContractNetResponderAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		initFSM();
	}

    private void send(final ACLMessage message) {
        message.send(getTransmitter());
        sent = message;
    }

	private void initFSM() {
		registerInitialState(CHECK_CFP, new StateAction() {

			@Override
			public String action() {
				final Iterable<ACLMessage> matches = getReceiver().pollMessages(createCFPTemplate());

				for (ACLMessage message : matches) {

					ACLMessage cfpReply = handleCFP(message);
					checkCFPReply(cfpReply);
					cfpReplies.add(cfpReply);
					send(cfpReply);
					if (cfpReply.matches(MessageTemplate.performative(ACLPerformative.PROPOSE)))
						++nExpectedProposeAnswers;
					
					ACLMessage.recycle(message);
				}

				timeoutCounter = 0;
				return nExpectedProposeAnswers > 0 ? WAIT_FOR_ACCEPT : END;
			}
		});

		registerState(WAIT_FOR_ACCEPT, new StateAction() {
			
			@Override
			public String action() {
				Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(createProposalReplyTemplate());
				for (ACLMessage receivedMessage : receivedMessages) {
					if (receivedMessage.matches(MessageTemplate.performative(
							ACLPerformative.ACCEPT_PROPOSAL))) {

						ACLMessage response = handleAccept(receivedMessage);
						checkAcceptReply(response);

                        send(response);
					}
					else {
						handleReject(receivedMessage);
					}
					--nExpectedProposeAnswers;
					
					ACLMessage.recycle(receivedMessage);
				}

				++timeoutCounter;
				
				return (nExpectedProposeAnswers == 0) ? END : (timeoutCounter == TIMEOUT_TIME) ? TIMEOUT : WAIT_FOR_ACCEPT;
			}
		});

		registerEndState(TIMEOUT, new StateAction() {

			@Override
			public String action() {
				if (GreyfishLogger.isDebugEnabled())
					GreyfishLogger.debug(ContractNetInitiatiorAction.class.getSimpleName() + ": Timeout");
				cfpReplies.clear();
				return TIMEOUT;
			}
		});
		
		registerEndState(END, new StateAction() {
			@Override
			public String action() {
				cfpReplies.clear();
				return END;
			}
		});
	}

	protected ACLMessageReceiver getReceiver() {
		return componentOwner.getInterface(MessageInterface.class);
	}

	protected ACLMessageTransmitter getTransmitter() {
		return componentOwner.getInterface(MessageInterface.class);
	}

	private final MessageTemplate createProposalReplyTemplate() {
		return MessageTemplate.and(
				MessageTemplate.isReply(cfpReplies),
				MessageTemplate.or(
						MessageTemplate.performative(ACLPerformative.ACCEPT_PROPOSAL),
						MessageTemplate.performative(ACLPerformative.REJECT_PROPOSAL)));
	}

	private final void checkCFPReply(ACLMessage response) {
		if (response == null)
			throw new NullPointerException();
		if (! response.matches(MessageTemplate.or(
				MessageTemplate.performative(ACLPerformative.PROPOSE),
				MessageTemplate.performative(ACLPerformative.REFUSE))))
			throw new AssertionError();
	}

	private final void checkAcceptReply(ACLMessage response) {
		if (response == null)
			throw new NullPointerException();
		if (! response.matches(MessageTemplate.or(
				MessageTemplate.performative(ACLPerformative.INFORM),
				MessageTemplate.performative(ACLPerformative.FAILURE))))
			throw new AssertionError();
	}

	protected abstract ACLMessage handleAccept(ACLMessage message);

	protected void handleReject(ACLMessage message) {
	}

	protected abstract ACLMessage handleCFP(ACLMessage message);
	
	protected MessageTemplate createCFPTemplate() {
        return MessageTemplate.performative(ACLPerformative.CFP);
    };
}
