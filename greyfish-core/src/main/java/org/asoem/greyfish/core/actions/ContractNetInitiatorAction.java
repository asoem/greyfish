package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ContractNetInitiatorAction<A extends Agent<A, ?>> extends FiniteStateAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractNetInitiatorAction.class);
    private static final int PROPOSAL_TIMEOUT_STEPS = 1;
    private static final int INFORM_TIMEOUT_STEPS = 1;

    private int timeoutCounter;
    private int nProposalsReceived;
    private int nInformReceived;

    protected ContractNetInitiatorAction(
            final AbstractBuilder<A,
                    ? extends ContractNetInitiatorAction<A>,
                    ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
    }

    protected ContractNetInitiatorAction(final ContractNetInitiatorAction<A> cloneable, final DeepCloner cloner) {
        super(cloneable, cloner);
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    private MessageTemplate template = MessageTemplates.alwaysFalse();
    private int nProposalsMax;

    @Override
    protected final Object initialState() {
        return State.SEND_CFP;
    }

    @Override
    protected final void executeState(final Object state) {
        if (State.SEND_CFP.equals(state)) {
            if (!canInitiate()) {
                endTransition(State.NO_RECEIVERS);
            } else {
                final ImmutableACLMessage<A> cfpMessage = createCFP()
                        .sender(agent())
                        .performative(ACLPerformative.CFP).build();
                LOGGER.debug("{}: Calling for proposals", this, cfpMessage);
                agent().sendMessage(cfpMessage);

                nProposalsMax = cfpMessage.getRecipients().size();
                timeoutCounter = 0;
                nProposalsReceived = 0;
                template = createCFPReplyTemplate(cfpMessage);

                transition(State.WAIT_FOR_PROPOSALS);
            }
        } else if (State.WAIT_FOR_PROPOSALS.equals(state)) {
            final Collection<ACLMessage<A>> proposeReplies = Lists.newArrayList();
            for (final ACLMessage<A> receivedMessage : agent().getMessages(getTemplate())) {
                assert (receivedMessage != null);

                ACLMessage<A> proposeReply;
                switch (receivedMessage.getPerformative()) {

                    case PROPOSE:
                        try {
                            proposeReply = checkNotNull(handlePropose(receivedMessage)).build();
                            proposeReplies.add(proposeReply);
                            ++nProposalsReceived;
                            LOGGER.debug("{}: Received proposal", this, receivedMessage);
                        } catch (NotUnderstoodException e) {
                            proposeReply = ImmutableACLMessage.createReply(receivedMessage, agent())
                                    .performative(ACLPerformative.NOT_UNDERSTOOD)
                                    .content(e.getMessage(), String.class).build();
                            LOGGER.warn("Message not understood {}", receivedMessage, e);
                        }
                        checkProposeReply(proposeReply);
                        LOGGER.debug("{}: Replying to proposal", this, proposeReply);
                        agent().sendMessage(proposeReply);
                        break;

                    case REFUSE:
                        LOGGER.debug("{}: CFP was refused: ", this, receivedMessage);
                        handleRefuse(receivedMessage);
                        --nProposalsMax;
                        break;

                    case NOT_UNDERSTOOD:
                        LOGGER.debug("{}: Communication Error: NOT_UNDERSTOOD received", this);
                        --nProposalsMax;
                        break;

                    default:
                        LOGGER.debug("{}: Protocol Error:"
                                + "Expected performative PROPOSE, REFUSE or NOT_UNDERSTOOD, received {}.",
                                this, receivedMessage.getPerformative());
                        --nProposalsMax;
                        break;
                }
            }

            ++timeoutCounter;

            assert nProposalsMax >= 0;

            if (nProposalsMax == 0) {
                LOGGER.debug("{}: received 0 proposals for {} CFP messages", this, nProposalsMax);
                endTransition(State.END);
            } else if (nProposalsReceived == nProposalsMax) {
                template = createAcceptReplyTemplate(proposeReplies);
                nInformReceived = 0;
                timeoutCounter = 0;
                transition(State.WAIT_FOR_INFORM);
            } else if (timeoutCounter > PROPOSAL_TIMEOUT_STEPS) {
                LOGGER.trace("{}: entered ACCEPT_TIMEOUT for accepting proposals. Received {} proposals",
                        this, nProposalsReceived);

                timeoutCounter = 0;
                failure("Timeout for INFORM Messages");
            }

            // else: No transition
        } else if (State.WAIT_FOR_INFORM.equals(state)) {
            assert timeoutCounter == 0 && nInformReceived == 0 || timeoutCounter != 0;

            for (final ACLMessage<A> receivedMessage : agent().getMessages(getTemplate())) {
                assert receivedMessage != null;

                switch (receivedMessage.getPerformative()) {

                    case INFORM:
                        handleInform(receivedMessage);
                        break;

                    case FAILURE:
                        LOGGER.debug("{}: Received FAILURE: {}", this, receivedMessage);
                        handleFailure(receivedMessage);
                        break;

                    case NOT_UNDERSTOOD:
                        LOGGER.debug("{}: Received NOT_UNDERSTOOD: {}", this, receivedMessage);
                        break;

                    default:
                        LOGGER.debug("{}: Expected none of INFORM, FAILURE or NOT_UNDERSTOOD: {}",
                                ContractNetInitiatorAction.this, receivedMessage);
                        break;
                }

                ++nInformReceived;
            }

            ++timeoutCounter;

            if (nInformReceived == nProposalsReceived) {
                endTransition(State.END);
            } else if (timeoutCounter > INFORM_TIMEOUT_STEPS) {
                failure("Timeout for INFORM Messages");
            }
        } else {
            throw unknownState();
        }
    }

    protected abstract boolean canInitiate();

    private static MessageTemplate createAcceptReplyTemplate(final Iterable<? extends ACLMessage<?>> acceptMessages) {
        if (Iterables.isEmpty(acceptMessages)) {
            return MessageTemplates.alwaysFalse();
        } else {
            return MessageTemplates.or( // is a reply
                    Iterables.toArray(
                            Iterables.transform(acceptMessages, new Function<ACLMessage, MessageTemplate>() {
                                @Override
                                public MessageTemplate apply(final ACLMessage aclMessage) {
                                    return MessageTemplates.isReplyTo(aclMessage);
                                }
                            }),
                            MessageTemplate.class));
        }
    }

    private static <A extends Agent<A, ?>> MessageTemplate createCFPReplyTemplate(final ACLMessage<A> cfp) {
        return MessageTemplates.isReplyTo(cfp);
    }

    private static void checkProposeReply(final ACLMessage response) {
        assert response != null;
        assert response.matches(MessageTemplates.or(
                MessageTemplates.performative(ACLPerformative.ACCEPT_PROPOSAL),
                MessageTemplates.performative(ACLPerformative.REJECT_PROPOSAL),
                MessageTemplates.performative(ACLPerformative.NOT_UNDERSTOOD)));
    }

    protected abstract ImmutableACLMessage.Builder<A> createCFP();

    protected abstract ImmutableACLMessage.Builder<A> handlePropose(ACLMessage<A> message);

    @SuppressWarnings("UnusedParameters") // hook method
    protected void handleRefuse(final ACLMessage<A> message) {}

    @SuppressWarnings("UnusedParameters") // hook method
    protected void handleFailure(final ACLMessage<A> message) {}

    protected void handleInform(final ACLMessage<A> message) {}

    protected abstract String getOntology();

    protected abstract static class AbstractBuilder<A extends Agent<A, ?>,
            C extends ContractNetInitiatorAction<A>,
            B extends AbstractBuilder<A, C, B>> extends FiniteStateAction.AbstractBuilder<A, C, B>
            implements Serializable {
    }

    private static enum State {
        SEND_CFP,
        WAIT_FOR_PROPOSALS,
        WAIT_FOR_INFORM,
        END,
        NO_RECEIVERS, TIMEOUT
    }
}
