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

public abstract class ContractNetResponderAction extends FSMAction {

    private static final String CHECK_CFP = "Check-cfp";
    private static final String WAIT_FOR_ACCEPT = "Wait-for-accept";
    private static final String END = "End";
    private static final String TIMEOUT = "Timeout";

    private static final int TIMEOUT_TIME = 10;

    private int timeoutCounter;
    private int nExpectedProposeAnswers;

    protected ContractNetResponderAction(ContractNetResponderAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        initFSM();
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    private MessageTemplate template = MessageTemplate.alwaysFalse();

    public ContractNetResponderAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
        initFSM();
    }

    private void initFSM() {
        registerInitialFSMState(CHECK_CFP, new StateAction() {

            @Override
            public String action() {
                final Iterable<ACLMessage> matches = getReceiver().pollMessages(createCFPTemplate(getOntology()));

                final Collection<ACLMessage> cfpReplies = new ArrayList<ACLMessage>();
                for (ACLMessage message : matches) {

                    ACLMessage cfpReply;
                    try {
                        cfpReply = handleCFP(message).build();
                    } catch (NotUnderstoodException e) {
                        cfpReply = message.replyFrom(componentOwner)
                                .performative(ACLPerformative.NOT_UNDERSTOOD)
                                .stringContent(e.getMessage()).build();
                        if (GreyfishLogger.isDebugEnabled())
                            GreyfishLogger.debug("Message not understood", e);
                    }
                    checkCFPReply(cfpReply);
                    cfpReplies.add(cfpReply);
                    send(cfpReply);

                    if (cfpReply.matches(MessageTemplate.performative(ACLPerformative.PROPOSE)))
                        ++nExpectedProposeAnswers;
                }

                template = createProposalReplyTemplate(cfpReplies);
                timeoutCounter = 0;
                return nExpectedProposeAnswers > 0 ? WAIT_FOR_ACCEPT : END;
            }
        });

        registerFSMState(WAIT_FOR_ACCEPT, new StateAction() {

            @Override
            public String action() {
                Iterable<ACLMessage> receivedMessages = getReceiver().pollMessages(getTemplate());
                for (ACLMessage receivedMessage : receivedMessages) {
                    // TODO: turn into switch statement
                    switch (receivedMessage.getPerformative()) {
                        case ACCEPT_PROPOSAL:
                            ACLMessage response = handleAccept(receivedMessage).build();
                            checkAcceptReply(response);
                            send(response);
                            break;
                        case REJECT_PROPOSAL:
                            handleReject(receivedMessage);
                            break;
                        case NOT_UNDERSTOOD:
                            if (GreyfishLogger.isDebugEnabled())
                                GreyfishLogger.debug("Communication Error: Message not understood");
                            break;
                        default:
                            if (GreyfishLogger.isDebugEnabled())
                                GreyfishLogger.debug("Protocol Error: Expected ACCEPT_PROPOSAL, REJECT_PROPOSAL or NOT_UNDERSTOOD. Received " + receivedMessage.getPerformative());
                            break;
                    }

                    --nExpectedProposeAnswers;
                }

                ++timeoutCounter;

                return (nExpectedProposeAnswers == 0) ? END :
                        (timeoutCounter == TIMEOUT_TIME) ? TIMEOUT : WAIT_FOR_ACCEPT;
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

    private void send(final ACLMessage message) {
        message.send(getTransmitter());
    }

    protected abstract String getOntology();

    protected ACLMessageReceiver getReceiver() {
        return componentOwner.getInterface(MessageInterface.class);
    }

    protected ACLMessageTransmitter getTransmitter() {
        return componentOwner.getInterface(MessageInterface.class);
    }

    private static MessageTemplate createProposalReplyTemplate(Collection<ACLMessage> cfpReplies) {
        return MessageTemplate.any(
                Iterables.toArray(
                        Iterables.transform(cfpReplies, new Function<ACLMessage, MessageTemplate>() {
                            @Override
                            public MessageTemplate apply(ACLMessage aclMessage) {
                                return MessageTemplate.isReplyTo(aclMessage);
                            }
                        }),
                        MessageTemplate.class));
    }

    private static void checkCFPReply(ACLMessage response) {
        assert (response != null);
        assert (response.matches(MessageTemplate.any(
                MessageTemplate.performative(ACLPerformative.PROPOSE),
                MessageTemplate.performative(ACLPerformative.REFUSE),
                MessageTemplate.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    private static void checkAcceptReply(ACLMessage response) {
        assert (response != null);
        assert (response.matches(MessageTemplate.any(
                MessageTemplate.performative(ACLPerformative.INFORM),
                MessageTemplate.performative(ACLPerformative.FAILURE),
                MessageTemplate.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    protected abstract ACLMessage.Builder handleAccept(ACLMessage message);

    protected void handleReject(ACLMessage message) {
    }

    protected abstract ACLMessage.Builder handleCFP(ACLMessage message) throws NotUnderstoodException;

    private static MessageTemplate createCFPTemplate(final String ontology) {
        return MessageTemplate.and(
                MessageTemplate.ontology(ontology),
                MessageTemplate.performative(ACLPerformative.CFP)
        );
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        checkState(!Strings.isNullOrEmpty(getOntology()));
    }
}
