package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.interfaces.MessageInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;

import java.util.ArrayList;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkState;

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

    public ContractNetInitiatiorAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
        initFSM();
    }

    protected ContractNetInitiatiorAction(ContractNetInitiatiorAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
    }

    private MessageTemplate template = MessageTemplate.alwaysFalse();
    private int nProposalsExpected;

    private void initFSM() {
        registerInitialFSMState(SEND_CFP, new StateAction() {

            @Override
            public String action() {
                ACLMessage cfpMessage = createCFP().performative(ACLPerformative.CFP).build();
                cfpMessage.send(getTransmitter());
                nProposalsExpected = cfpMessage.getAllReceiver().size();
                timeoutCounter = 0;
                nReceivedProposals = 0;
                template = createCFPReplyTemplate(cfpMessage);

                return WAIT_FOR_POROPOSALS;
            }
        });

        registerFSMState(WAIT_FOR_POROPOSALS, new StateAction() {

            @Override
            public String action() {
                Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(getTemplate());

                Collection<ACLMessage> proposeReplies = new ArrayList<ACLMessage>();
                for (ACLMessage receivedMessage : receivedMessages) {

                    ACLMessage proposeReply = null;
                    switch (receivedMessage.getPerformative()) {
                        case PROPOSE:

                            try {
                                proposeReply = handlePropose(receivedMessage).build();
                                proposeReplies.add(proposeReply);
                                ++nReceivedProposals;
                            } catch (NotUnderstoodException e) {
                                proposeReply = receivedMessage.replyFrom(componentOwner)
                                        .performative(ACLPerformative.NOT_UNDERSTOOD)
                                        .stringContent(e.getMessage()).build();
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
                }
                ++timeoutCounter;


                assert nProposalsExpected >= 0;

                if (nProposalsExpected == 0)
                    return END;
                else if (timeoutCounter == PROPOSAL_TIMEOUT) {
                    timeoutCounter = 0;
                    return WAIT_FOR_INFORM;
                }
                else {
                    template = createAcceptReplyTemplate(proposeReplies);
                    return (nReceivedProposals != nProposalsExpected) ? WAIT_FOR_POROPOSALS : WAIT_FOR_INFORM;
                }
            }
        });

        registerFSMState(WAIT_FOR_INFORM, new StateAction() {

            @Override
            public String action() {
                Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(getTemplate());
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
                }
                ++timeoutCounter;
                if (nReceivedAcceptAnswers == nReceivedProposals)
                    return END;
                else if (timeoutCounter == INFORM_TIMEOUT)
                    return TIMEOUT;
                else
                    return WAIT_FOR_INFORM;
            }
        });

        registerEndFSMState(TIMEOUT, new StateAction() {

            @Override
            public String action() {
                if (GreyfishLogger.isDebugEnabled())
                    GreyfishLogger.debug(ContractNetInitiatiorAction.class.getSimpleName() + ": Timeout");
                return TIMEOUT;
            }
        });

        registerEndFSMState(END, new StateAction() {

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

    protected abstract ACLMessage.Builder createCFP();

    protected abstract ACLMessage.Builder handlePropose(ACLMessage message) throws NotUnderstoodException;

    protected void handleRefuse(ACLMessage message) {
    }

    protected void handleFailure(ACLMessage message) {
    }

    protected void handleInform(ACLMessage message) {
    }

    protected abstract String getOntology();

    @Override
    public void checkIfFreezable(Iterable<? extends GFComponent> components) {
        super.checkIfFreezable(components);
        checkState(!Strings.isNullOrEmpty(getOntology()));
    }
}