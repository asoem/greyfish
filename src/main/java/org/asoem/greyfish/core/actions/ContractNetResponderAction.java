package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.utils.CloneMap;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.io.GreyfishLogger.debug;
import static org.asoem.greyfish.core.io.GreyfishLogger.isDebugEnabled;

public abstract class ContractNetResponderAction extends FSMAction {

    private static final String CHECK_CFP = "Check-cfp";
    private static final String WAIT_FOR_ACCEPT = "Wait-for-accept";
    private static final String END = "End";
    private static final String TIMEOUT = "Timeout";

    private static final int TIMEOUT_TIME = 1;

    private int timeoutCounter;
    private int nExpectedProposeAnswers;
    private MessageTemplate template = MessageTemplate.alwaysFalse();

    protected ContractNetResponderAction(ContractNetResponderAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        initFSM();
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    public ContractNetResponderAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
        initFSM();
    }

    private void initFSM() {
        registerInitialFSMState(CHECK_CFP, new StateAction() {

            @Override
            public String action() {
                template = createCFPTemplate(getOntology());

                final List<ACLMessage> cfpReplies = Lists.newArrayList();
                for (ACLMessage message : receiveMessages(getTemplate())) {

                    ACLMessage cfpReply;
                    try {
                        cfpReply = handleCFP(message).build();
                    } catch (NotUnderstoodException e) {
                        cfpReply = message.replyFrom(getComponentOwner().getId())
                                .performative(ACLPerformative.NOT_UNDERSTOOD)
                                .stringContent(e.getMessage()).build();
                        if (isDebugEnabled())
                            debug("Message not understood", e);
                    }
                    checkCFPReply(cfpReply);
                    cfpReplies.add(cfpReply);
                    sendMessage(cfpReply);

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
                Iterable<ACLMessage> receivedMessages = receiveMessages(getTemplate());
                for (ACLMessage receivedMessage : receivedMessages) {
                    // TODO: turn into switch statement
                    switch (receivedMessage.getPerformative()) {
                        case ACCEPT_PROPOSAL:
                            ACLMessage response = handleAccept(receivedMessage).build();
                            checkAcceptReply(response);
                            sendMessage(response);
                            break;
                        case REJECT_PROPOSAL:
                            handleReject(receivedMessage);
                            break;
                        case NOT_UNDERSTOOD:
                            if (isDebugEnabled())
                                debug("Communication Error: Message not understood");
                            break;
                        default:
                            if (isDebugEnabled())
                                debug("Protocol Error: Expected ACCEPT_PROPOSAL, REJECT_PROPOSAL or NOT_UNDERSTOOD." +
                                        "Received " + receivedMessage.getPerformative());
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
                if (isDebugEnabled())
                    debug(ContractNetInitiatiorAction.class.getSimpleName() + ": TIMEOUT");
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

    protected abstract String getOntology();

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
