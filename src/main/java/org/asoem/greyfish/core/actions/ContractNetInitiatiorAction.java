package org.asoem.greyfish.core.actions;

import java.util.Collection;
import java.util.Map;

import javolution.util.FastList;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLMessageReceiver;
import org.asoem.greyfish.core.acl.ACLMessageTransmitter;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.interfaces.MessageInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

public abstract class ContractNetInitiatiorAction extends FSMAction {

	private static final String SEND_CFP = "Send-cfp";
	private static final String WAIT_FOR_POROPOSALS = "Wait-for-proposals";
	private static final String WAIT_FOR_INFORM = "Wait-for-inform";
	private static final String END = "End";
	private static final String TIMEOUT = "Timeout";

	private static final int PROPOSAL_TIMEOUT = 10;
	private static final int INFORM_TIMEOUT = 10;

	private int timeoutCounter;
	private int nReceivedProposals;
	private int nReceivedAcceptAnswers;

	private ACLMessage cfpMessage;
	private final Collection<ACLMessage> proposeReplies = new FastList<ACLMessage>();
	private int nProposalsExpected;

	public ContractNetInitiatiorAction() {
		initFSM();
	}

	public ContractNetInitiatiorAction(String name) {
		super(name);
		initFSM();
	}

	public ContractNetInitiatiorAction(ContractNetInitiatiorAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		initFSM();
	}

	private void initFSM() {
		registerInitialState(SEND_CFP, new StateAction() {

			@Override
			public String action() {
				cfpMessage = createCFP();
				checkCFP(cfpMessage);

				cfpMessage.send(getTransmitter());
				nProposalsExpected = cfpMessage.getAllReceiver().size();
				timeoutCounter = 0;
				nReceivedProposals = 0;
				
				return WAIT_FOR_POROPOSALS;
			}
		});

		registerState(WAIT_FOR_POROPOSALS, new StateAction() {

			@Override
			public String action() {
				Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(createCFPReplyTemplate());
				
				for (ACLMessage receivedMessage : receivedMessages) {
					if (receivedMessage.matches(MessageTemplate.performative(
							ACLPerformative.PROPOSE))) {

						ACLMessage proposeReply = handlePropose(receivedMessage); // TODO: Wait for all proposals, than handle them all at once
						checkProposeReply(proposeReply);
						proposeReplies.add(proposeReply);
						++nReceivedProposals;

						proposeReply.send(getTransmitter());
					}
					else {
						handleRefuse(receivedMessage);
					}
					
					ACLMessage.recycle(receivedMessage);
				}
				++timeoutCounter;
				if (timeoutCounter == PROPOSAL_TIMEOUT) {
					timeoutCounter = 0;
					return WAIT_FOR_INFORM;
				}
				
				return (nReceivedProposals != nProposalsExpected) ? WAIT_FOR_POROPOSALS : WAIT_FOR_INFORM;
			}
		});

		registerState(WAIT_FOR_INFORM, new StateAction() {

			@Override
			public String action() {
				Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(createCFPReplyTemplate());
				for (ACLMessage receivedMessage : receivedMessages) {
					if (receivedMessage.matches(MessageTemplate.performative(
							ACLPerformative.INFORM))) {
						handleInform(receivedMessage);
					}
					else {
						handleFailure(receivedMessage);
					}
					++nReceivedAcceptAnswers;
					ACLMessage.recycle(receivedMessage);
				}
				++timeoutCounter;
				return (nReceivedAcceptAnswers == nReceivedProposals
						|| timeoutCounter == INFORM_TIMEOUT) ? TIMEOUT : WAIT_FOR_INFORM;
			}
		});

		registerEndState(TIMEOUT, new StateAction() {

			@Override
			public String action() {
				if (GreyfishLogger.isDebugEnabled())
					GreyfishLogger.debug(ContractNetInitiatiorAction.class.getSimpleName() + ": Timeout");
				return TIMEOUT;
			}
		});
		
		registerEndState(END, new StateAction() {

			@Override
			public String action() {
				proposeReplies.clear();
				return END;
			}
		});
	}

	private final MessageTemplate createAcceptReplyTemplate() {
		return MessageTemplate.and(
				MessageTemplate.isReply(proposeReplies),
				MessageTemplate.or(
						MessageTemplate.performative(ACLPerformative.INFORM),
						MessageTemplate.performative(ACLPerformative.FAILURE)));
	}

	private final MessageTemplate createCFPReplyTemplate() {
		return MessageTemplate.and(
				MessageTemplate.inReplyTo(cfpMessage.getReplyWith()),
				MessageTemplate.or(
						MessageTemplate.performative(ACLPerformative.PROPOSE),
						MessageTemplate.performative(ACLPerformative.REFUSE)));
	}

	private final void checkCFP(ACLMessage cfpMessage) {
		if (cfpMessage == null)
			throw new NullPointerException();
		// TODO: add sender, receiver, etc. ?
		if ( ! cfpMessage.matches(MessageTemplate.performative(ACLPerformative.CFP)))
			throw new AssertionError();
	}

	private final void checkProposeReply(ACLMessage response) {
		if (response == null)
			throw new NullPointerException();
		if (! response.matches(MessageTemplate.any(
				MessageTemplate.performative(ACLPerformative.ACCEPT_PROPOSAL),
				MessageTemplate.performative(ACLPerformative.REJECT_PROPOSAL),
				MessageTemplate.performative(ACLPerformative.NOT_UNDERSTOOD))))
			throw new AssertionError();
	}
	
	protected ACLMessageReceiver getReceiver() {
		return componentOwner.getInterface(MessageInterface.class);
	}

	protected ACLMessageTransmitter getTransmitter() {
		return componentOwner.getInterface(MessageInterface.class);
	}

	protected abstract ACLMessage createCFP();

	protected abstract ACLMessage handlePropose(ACLMessage message);

	protected void handleRefuse(ACLMessage message) {
	}

	protected void handleFailure(ACLMessage message) {
	}

	protected void handleInform(ACLMessage message) {
	}
}
