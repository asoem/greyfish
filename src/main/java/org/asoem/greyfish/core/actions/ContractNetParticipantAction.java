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
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.utils.CloneMap;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public abstract class ContractNetParticipantAction extends FiniteStateAction {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ContractNetParticipantAction.class);

    private static enum State {
        CHECK_CFP,
        WAIT_FOR_ACCEPT,
        END,
        TIMEOUT,
        NO_ACCEPT,
        NO_PROPOSE
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

    private void initFSM() {
        registerInitialState(State.CHECK_CFP, new StateAction() {

            @Override
            public Object run(ActionContext context) {
                template = createCFPTemplate(getOntology());

                final List<ACLMessage> cfpReplies = Lists.newArrayList();
                for (ACLMessage message : context.receiveMessages(template)) {

                    ACLMessage cfpReply;
                    try {
                        cfpReply = handleCFP(message).build();
                    } catch (NotUnderstoodException e) {
                        cfpReply = message.createReplyFrom(getComponentOwner().getId())
                                .performative(ACLPerformative.NOT_UNDERSTOOD)
                                .stringContent(e.getMessage()).build();
                        LOGGER.debug("Message not understood", e);
                    }
                    checkCFPReply(cfpReply);
                    cfpReplies.add(cfpReply);
                    context.deliverMessage(cfpReply);

                    if (cfpReply.matches(MessageTemplate.performative(ACLPerformative.PROPOSE)))
                        ++nExpectedProposeAnswers;
                }

                template = createProposalReplyTemplate(cfpReplies);
                timeoutCounter = 0;
                return nExpectedProposeAnswers > 0 ? State.WAIT_FOR_ACCEPT : State.NO_PROPOSE;
            }
        });

        registerIntermediateState(State.WAIT_FOR_ACCEPT, new StateAction() {

            @Override
            public Object run(ActionContext context) {
                Iterable<ACLMessage> receivedMessages = context.receiveMessages(getTemplate());
                for (ACLMessage receivedMessage : receivedMessages) {
                    // TODO: turn into switch statement
                    switch (receivedMessage.getPerformative()) {
                        case ACCEPT_PROPOSAL:
                            ACLMessage response;
                            try {
                                response = handleAccept(receivedMessage).build();
                            } catch (NotUnderstoodException e) {
                                response = receivedMessage.createReplyFrom(getComponentOwner().getId())
                                        .performative(ACLPerformative.NOT_UNDERSTOOD)
                                        .stringContent(e.getMessage()).build();

                                LOGGER.debug("Message not understood", e);
                            }
                            checkAcceptReply(response);
                            context.deliverMessage(response);
                            break;
                        case REJECT_PROPOSAL:
                            handleReject(receivedMessage);
                            break;
                        case NOT_UNDERSTOOD:
                            LOGGER.debug("Communication Error: Message not understood");
                            break;
                        default:
                            LOGGER.debug("Protocol Error: Expected ACCEPT_PROPOSAL, REJECT_PROPOSAL or NOT_UNDERSTOOD. Received {}", receivedMessage.getPerformative());
                            break;
                    }

                    --nExpectedProposeAnswers;
                }

                ++timeoutCounter;

                return (nExpectedProposeAnswers == 0) ? State.NO_ACCEPT :
                        (timeoutCounter > TIMEOUT_ACCEPT_STEPS) ? State.TIMEOUT : State.WAIT_FOR_ACCEPT;
            }
        });

        registerFailureState(State.TIMEOUT, new EndStateAction(State.TIMEOUT));
        registerFailureState(State.NO_PROPOSE, new EndStateAction(State.NO_PROPOSE));
        registerFailureState(State.NO_ACCEPT, new EndStateAction(State.NO_ACCEPT));
        registerEndState(State.END, new EndStateAction(State.END));
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
