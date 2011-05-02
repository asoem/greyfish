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
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.ContractNetParticipantAction.States.*;
import static org.asoem.greyfish.core.io.GreyfishLogger.GFACTIONS_LOGGER;

public abstract class ContractNetParticipantAction extends FiniteStateAction {

    enum States {
        CHECK_CFP,
        WAIT_FOR_ACCEPT,
        END,
        TIMEOUT
    }

    private static final int TIMEOUT_ACCEPT_STEPS = 1;

    private int timeoutCounter;
    private int nExpectedProposeAnswers;
    private MessageTemplate template = MessageTemplate.alwaysFalse();

    protected ContractNetParticipantAction(ContractNetParticipantAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        initFSM();
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    public ContractNetParticipantAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
        initFSM();
    }

    @Override
    protected boolean evaluateInternalState(Simulation simulation) {
        return !isResuming() && hasMessages(createCFPTemplate(getOntology()))
                && super.evaluateInternalState(simulation);
    }

    private void initFSM() {
        registerInitialState(CHECK_CFP, new StateAction() {

            @Override
            public Object run() {
                template = createCFPTemplate(getOntology());

                final List<ACLMessage> cfpReplies = Lists.newArrayList();
                for (ACLMessage message : receiveMessages(getTemplate())) {

                    ACLMessage cfpReply;
                    try {
                        cfpReply = handleCFP(message).build();
                    } catch (NotUnderstoodException e) {
                        cfpReply = message.createReplyFrom(getComponentOwner().getId())
                                .performative(ACLPerformative.NOT_UNDERSTOOD)
                                .stringContent(e.getMessage()).build();
                        GFACTIONS_LOGGER.debug("Message not understood", e);
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

        registerState(WAIT_FOR_ACCEPT, new StateAction() {

            @Override
            public Object run() {
                Iterable<ACLMessage> receivedMessages = receiveMessages(getTemplate());
                for (ACLMessage receivedMessage : receivedMessages) {
                    // TODO: turn into switch statement
                    switch (receivedMessage.getPerformative()) {
                        case ACCEPT_PROPOSAL:
                            ACLMessage response = null;
                            try {
                                response = handleAccept(receivedMessage).build();
                            } catch (NotUnderstoodException e) {
                                response = receivedMessage.createReplyFrom(getComponentOwner().getId())
                                        .performative(ACLPerformative.NOT_UNDERSTOOD)
                                        .stringContent(e.getMessage()).build();

                                GFACTIONS_LOGGER.debug("Message not understood", e);
                            }
                            checkAcceptReply(response);
                            sendMessage(response);
                            break;
                        case REJECT_PROPOSAL:
                            handleReject(receivedMessage);
                            break;
                        case NOT_UNDERSTOOD:
                            GFACTIONS_LOGGER.debug("Communication Error: Message not understood");
                            break;
                        default:
                            GFACTIONS_LOGGER.debug("Protocol Error: Expected ACCEPT_PROPOSAL, REJECT_PROPOSAL or NOT_UNDERSTOOD. Received {}", receivedMessage.getPerformative());
                            break;
                    }

                    --nExpectedProposeAnswers;
                }

                ++timeoutCounter;

                return (nExpectedProposeAnswers == 0) ? END :
                        (timeoutCounter > TIMEOUT_ACCEPT_STEPS) ? TIMEOUT : WAIT_FOR_ACCEPT;
            }
        });

        registerErrorState(TIMEOUT, new StateAction() {

            @Override
            public Object run() {
                GFACTIONS_LOGGER.debug("TIMEOUT");
                return TIMEOUT;
            }
        });

        registerEndState(END, new StateAction() {
            @Override
            public Object run() {
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

    protected abstract ACLMessage.Builder handleAccept(ACLMessage message) throws NotUnderstoodException;

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
