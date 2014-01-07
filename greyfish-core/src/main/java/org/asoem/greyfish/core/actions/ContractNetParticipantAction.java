package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ContractNetParticipantAction<A extends Agent<?>> extends FiniteStateAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractNetParticipantAction.class);
    private static final int TIMEOUT_ACCEPT_STEPS = 1;
    private int timeoutCounter;
    private int nExpectedProposeAnswers;
    private MessageTemplate template = MessageTemplates.alwaysFalse();

    protected ContractNetParticipantAction(final AbstractBuilder<A, ? extends ContractNetParticipantAction<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.timeoutCounter = builder.timeoutCounter;
        this.nExpectedProposeAnswers = builder.nExpectedProposeAnswers;
        this.template = builder.template;
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    @Override
    protected Object initialState() {
        return State.CHECK_CFP;
    }

    @Override
    protected void executeState(final Object state, final AgentContext<A> context) {

        if (State.CHECK_CFP == state) {
            prepareForCommunication();
            template = createCFPTemplate(getOntology());

            final List<ACLMessage<A>> cfpReplies = Lists.newArrayList();
            final Iterable<ACLMessage<A>> proposalCalls = context.getMessages(template);
            for (final ACLMessage<A> cfp : proposalCalls) {

                ACLMessage<A> cfpReply;
                try {
                    cfpReply = checkNotNull(handleCFP(cfp, context)).build();
                } catch (NotUnderstoodException e) {
                    cfpReply = ImmutableACLMessage.createReply(cfp, context.agent())
                            .performative(ACLPerformative.NOT_UNDERSTOOD)
                            .content(e.getMessage()).build();
                    LOGGER.warn("Message not understood {}", cfp, e);
                }
                checkCFPReply(cfpReply);
                cfpReplies.add(cfpReply);
                LOGGER.debug("{}: Replying to CFP with {}", this, cfpReply);
                context.sendMessage(cfpReply);

                if (cfpReply.matches(MessageTemplates.performative(ACLPerformative.PROPOSE)))
                    ++nExpectedProposeAnswers;
            }

            if (nExpectedProposeAnswers > 0) {
                template = createProposalReplyTemplate(cfpReplies);
                timeoutCounter = 0;
                transition(State.WAIT_FOR_ACCEPT);
            } else {
                endTransition(State.NO_CFP);
            }
        } else if (State.WAIT_FOR_ACCEPT == state) {
            final Iterable<ACLMessage<A>> receivedMessages = context.getMessages(getTemplate());
            for (final ACLMessage<A> receivedMessage : receivedMessages) {
                switch (receivedMessage.getPerformative()) {
                    case ACCEPT_PROPOSAL:
                        ACLMessage<A> informMessage;
                        try {
                            informMessage = handleAccept(receivedMessage, context).build();
                        } catch (NotUnderstoodException e) {
                            informMessage = ImmutableACLMessage.createReply(receivedMessage, context.agent())
                                    .performative(ACLPerformative.NOT_UNDERSTOOD)
                                    .content(e.getMessage()).build();

                            LOGGER.warn("Message not understood {}", receivedMessage, e);
                        }
                        checkAcceptReply(informMessage);
                        LOGGER.debug("{}: Accepting proposal", this);
                        context.sendMessage(informMessage);
                        break;
                    case REJECT_PROPOSAL:
                        handleReject(receivedMessage);
                        break;
                    default:
                        throw new AssertionError("Received message with unexpected performative: " + receivedMessage.getPerformative());
                }

                --nExpectedProposeAnswers;
            }

            ++timeoutCounter;

            if (nExpectedProposeAnswers == 0)
                endTransition(State.END);
            else {
                if (timeoutCounter > TIMEOUT_ACCEPT_STEPS)
                    failure("Timeout for ACCEPT messages");
                else
                    transition(State.WAIT_FOR_ACCEPT);
            }
        } else
            throw unknownState();
    }

    protected abstract String getOntology();

    private static MessageTemplate createProposalReplyTemplate(final Iterable<? extends ACLMessage> cfpReplies) {
        return MessageTemplates.or(
                Iterables.toArray(
                        Iterables.transform(cfpReplies, new Function<ACLMessage, MessageTemplate>() {
                            @Override
                            public MessageTemplate apply(final ACLMessage aclMessage) {
                                return MessageTemplates.isReplyTo(aclMessage);
                            }
                        }),
                        MessageTemplate.class));
    }

    private static void checkCFPReply(final ACLMessage response) {
        assert (response != null);
        assert (response.matches(MessageTemplates.or(
                MessageTemplates.performative(ACLPerformative.PROPOSE),
                MessageTemplates.performative(ACLPerformative.REFUSE),
                MessageTemplates.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    private static void checkAcceptReply(final ACLMessage response) {
        assert (response != null);
        assert (response.matches(MessageTemplates.or(
                MessageTemplates.performative(ACLPerformative.INFORM),
                MessageTemplates.performative(ACLPerformative.FAILURE),
                MessageTemplates.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    protected void prepareForCommunication() {
    }

    protected abstract ImmutableACLMessage.Builder<A> handleAccept(ACLMessage<A> message, final AgentContext<A> context);

    @SuppressWarnings("UnusedParameters") // hook method
    protected void handleReject(final ACLMessage<A> message) {
    }

    protected abstract ImmutableACLMessage.Builder<A> handleCFP(ACLMessage<A> message, final AgentContext<A> context);

    private static MessageTemplate createCFPTemplate(final String ontology) {
        assert ontology != null;
        return MessageTemplates.and(
                MessageTemplates.ontology(ontology),
                MessageTemplates.performative(ACLPerformative.CFP)
        );
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, C extends ContractNetParticipantAction<A>, B extends AbstractBuilder<A, C, B>> extends FiniteStateAction.AbstractBuilder<A, C, B> implements Serializable {
        private int timeoutCounter;
        private int nExpectedProposeAnswers;
        private MessageTemplate template = MessageTemplates.alwaysFalse();

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final ContractNetParticipantAction<A> action) {
            super(action);
            this.timeoutCounter = action.timeoutCounter;
            this.nExpectedProposeAnswers = action.nExpectedProposeAnswers;
            this.template = action.template;
        }
    }

    private static enum State {
        CHECK_CFP,
        WAIT_FOR_ACCEPT,
        END,
        ACCEPT_TIMEOUT,
        NO_CFP
    }
}
