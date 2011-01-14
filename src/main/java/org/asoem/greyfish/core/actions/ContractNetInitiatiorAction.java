package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.interfaces.MessageInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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

    private MessageTemplate template = MessageTemplate.alwaysFalse();
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
                ACLMessage cfpMessage = createCFP();

                checkCFP(cfpMessage);

                cfpMessage.send(getTransmitter());
                nProposalsExpected = cfpMessage.getAllReceiver().size();
                timeoutCounter = 0;
                nReceivedProposals = 0;
                template = createCFPReplyTemplate(cfpMessage);

                return WAIT_FOR_POROPOSALS;
            }
        });

        registerState(WAIT_FOR_POROPOSALS, new StateAction() {

            @Override
            public String action() {
                Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(template);

                Collection<ACLMessage> proposeReplies = new ArrayList<ACLMessage>();
                for (ACLMessage receivedMessage : receivedMessages) {

                    ACLMessage proposeReply = null;
                    switch (receivedMessage.getPerformative()) {
                        case PROPOSE:

                            try {
                                proposeReply = handlePropose(receivedMessage);
                                proposeReplies.add(proposeReply);
                                ++nReceivedProposals;
                            } catch (NotUnderstoodException e) {
                                proposeReply = e.createReply(receivedMessage);
                                if (GreyfishLogger.isDebugEnabled())
                                    GreyfishLogger.debug("Message not understood", e);
                            } finally {
                                assert proposeReply != null;
                            }
                            checkProposeReply(proposeReply);
                            proposeReply.send(getTransmitter());
                            break;

                        case REFUSE:
                            if (GreyfishLogger.isDebugEnabled())
                                GreyfishLogger.debug("CFP was refused: " + receivedMessage);
                            handleRefuse(receivedMessage);
                            --nProposalsExpected;
                            break;
                        case NOT_UNDERSTOOD:
                            if (GreyfishLogger.isDebugEnabled())
                                GreyfishLogger.debug("Communication Error: NOT_UNDERSTOOD received");
                            --nProposalsExpected;
                            break;
                        default:
                            if (GreyfishLogger.isDebugEnabled())
                                GreyfishLogger.debug("Protocol Error: Expected PROPOSE, REFUSE or NOT_UNDERSTOOD, received " + receivedMessage.getPerformative());
                            --nProposalsExpected;
                            break;

                    }

                    ACLMessage.recycle(receivedMessage);
                }
                ++timeoutCounter;
                if (timeoutCounter == PROPOSAL_TIMEOUT) {
                    timeoutCounter = 0;
                    return WAIT_FOR_INFORM;
                }

                template = createAcceptReplyTemplate(proposeReplies);
                assert nProposalsExpected >= 0;
                if (nProposalsExpected == 0)
                    return END;
                else
                    return (nReceivedProposals != nProposalsExpected) ? WAIT_FOR_POROPOSALS : WAIT_FOR_INFORM;
            }
        });

        registerState(WAIT_FOR_INFORM, new StateAction() {

            @Override
            public String action() {
                Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(template);
                for (ACLMessage receivedMessage : receivedMessages) {
                    switch (receivedMessage.getPerformative()) {
                        case INFORM:
                            handleInform(receivedMessage);
                            break;
                        case FAILURE:
                            handleFailure(receivedMessage);
                            break;
                        case NOT_UNDERSTOOD:
                            if (GreyfishLogger.isDebugEnabled())
                                GreyfishLogger.debug("Communication Error: NOT_UNDERSTOOD received");
                            break;
                        default:
                            if (GreyfishLogger.isDebugEnabled())
                                GreyfishLogger.debug("Protocol Error: Expected INFORM, FAILURE or NOT_UNDERSTOOD, received " + receivedMessage.getPerformative());
                            break;
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
                return END;
            }
        });
    }

    private static MessageTemplate createAcceptReplyTemplate(final Iterable<ACLMessage> acceptMessages) {
        return MessageTemplate.any( // is a reply
                Iterables.toArray(
                        Iterables.transform(acceptMessages, new Function<ACLMessage, MessageTemplate>() {
                            @Override
                            public MessageTemplate apply(ACLMessage aclMessage) {
                                return MessageTemplate.isReplyTo(aclMessage);
                            }
                        }),
                        MessageTemplate.class));
    }

    private static MessageTemplate createCFPReplyTemplate(final ACLMessage cfp) {
        return MessageTemplate.isReplyTo(cfp);
    }

    private static void checkCFP(ACLMessage cfpMessage) {
        assert cfpMessage != null : "Message must not be null";
//        assert ! Strings.isNullOrEmpty(cfpMessage.getReplyWith()) : "Message has invalid field reply-with: " + String.valueOf(cfpMessage.getReplyWith());
        assert cfpMessage.matches(MessageTemplate.performative(ACLPerformative.CFP)) : "Message must have performative set to CFP";
        // TODO: add sender, receiver, etc. ?
    }

    private static void checkProposeReply(ACLMessage response) {
        assert(response != null);
        assert(response.matches(MessageTemplate.any(
                MessageTemplate.performative(ACLPerformative.ACCEPT_PROPOSAL),
                MessageTemplate.performative(ACLPerformative.REJECT_PROPOSAL),
                MessageTemplate.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    protected ACLMessageReceiver getReceiver() {
        return componentOwner.getInterface(MessageInterface.class);
    }

    protected ACLMessageTransmitter getTransmitter() {
        return componentOwner.getInterface(MessageInterface.class);
    }

    protected abstract ACLMessage createCFP();

    protected abstract ACLMessage handlePropose(ACLMessage message) throws NotUnderstoodException;

    protected void handleRefuse(ACLMessage message) {
    }

    protected void handleFailure(ACLMessage message) {
    }

    protected void handleInform(ACLMessage message) {
    }

    protected abstract String getOntology();
}
