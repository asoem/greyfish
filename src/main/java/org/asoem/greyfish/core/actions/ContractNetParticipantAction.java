package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.DeepCloner;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ContractNetParticipantAction extends FiniteStateAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractNetParticipantAction.class);

    private static enum State {
        CHECK_CFP,
        WAIT_FOR_ACCEPT,
        END,
        ACCEPT_TIMEOUT,
        NO_CFP
    }

    private static final int TIMEOUT_ACCEPT_STEPS = 1;

    private int timeoutCounter;
    private int nExpectedProposeAnswers;
    private MessageTemplate template = MessageTemplates.alwaysFalse();

    protected ContractNetParticipantAction(ContractNetParticipantAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    public ContractNetParticipantAction(AbstractGFAction.AbstractBuilder<?,?> builder) {
        super(builder);
    }

    @Override
    protected Object initialState() {
        return State.CHECK_CFP;
    }

    @Override
    protected StateClass executeState(Object state, Simulation simulation) {
        if (State.CHECK_CFP.equals(state)) {
            template = createCFPTemplate(getOntology());

            final List<ACLMessage> cfpReplies = Lists.newArrayList();
            for (ACLMessage message : agent.get().pullMessages(template)) {

                ACLMessage cfpReply;
                try {
                    cfpReply = checkNotNull(handleCFP(message)).build();
                } catch (NotUnderstoodException e) {
                    cfpReply = ImmutableACLMessage.replyTo(message, agent.get().getId())
                            .performative(ACLPerformative.NOT_UNDERSTOOD)
                            .stringContent(e.getMessage()).build();
                    LOGGER.debug("Message not understood", e);
                }
                checkCFPReply(cfpReply);
                cfpReplies.add(cfpReply);
                agent.get().sendMessage(cfpReply);

                if (cfpReply.matches(MessageTemplates.performative(ACLPerformative.PROPOSE)))
                    ++nExpectedProposeAnswers;
            }

            template = createProposalReplyTemplate(cfpReplies);
            timeoutCounter = 0;
            return nExpectedProposeAnswers > 0
                    ? transition(State.WAIT_FOR_ACCEPT)
                    : endTransition(State.NO_CFP);
        }
        else if (State.WAIT_FOR_ACCEPT.equals(state)) {
             Iterable<ACLMessage> receivedMessages = agent.get().pullMessages(getTemplate());
                for (ACLMessage receivedMessage : receivedMessages) {
                    // TODO: turn into switch statement
                    switch (receivedMessage.getPerformative()) {
                        case ACCEPT_PROPOSAL:
                            ACLMessage response;
                            try {
                                response = handleAccept(receivedMessage).build();
                            } catch (NotUnderstoodException e) {
                                response = ImmutableACLMessage.replyTo(receivedMessage, agent.get().getId())
                                        .performative(ACLPerformative.NOT_UNDERSTOOD)
                                        .stringContent(e.getMessage()).build();

                                LOGGER.debug("Message not understood", e);
                            }
                            checkAcceptReply(response);
                            agent.get().sendMessage(response);
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

                return (nExpectedProposeAnswers == 0)
                        ? endTransition(State.END)
                        : (timeoutCounter > TIMEOUT_ACCEPT_STEPS)
                                ? failure(State.ACCEPT_TIMEOUT)
                                : transition(State.WAIT_FOR_ACCEPT);
        }

        throw unknownState();
    }

    protected abstract String getOntology();

    private static MessageTemplate createProposalReplyTemplate(Iterable<? extends ACLMessage> cfpReplies) {
        return MessageTemplates.or(
                Iterables.toArray(
                        Iterables.transform(cfpReplies, new Function<ACLMessage, MessageTemplate>() {
                            @Override
                            public MessageTemplate apply(ACLMessage aclMessage) {
                                return MessageTemplates.isReplyTo(aclMessage);
                            }
                        }),
                        MessageTemplate.class));
    }

    private static void checkCFPReply(ACLMessage response) {
        assert (response != null);
        assert (response.matches(MessageTemplates.or(
                MessageTemplates.performative(ACLPerformative.PROPOSE),
                MessageTemplates.performative(ACLPerformative.REFUSE),
                MessageTemplates.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    private static void checkAcceptReply(ACLMessage response) {
        assert (response != null);
        assert (response.matches(MessageTemplates.or(
                MessageTemplates.performative(ACLPerformative.INFORM),
                MessageTemplates.performative(ACLPerformative.FAILURE),
                MessageTemplates.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    protected abstract ImmutableACLMessage.Builder handleAccept(ACLMessage message) throws NotUnderstoodException;

    protected void handleReject(ACLMessage message) {
    }

    protected abstract ImmutableACLMessage.Builder handleCFP(ACLMessage message) throws NotUnderstoodException;

    private static MessageTemplate createCFPTemplate(final String ontology) {
        return MessageTemplates.and(
                MessageTemplates.ontology(ontology),
                MessageTemplates.performative(ACLPerformative.CFP)
        );
    }

}
