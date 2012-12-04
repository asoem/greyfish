package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentMessage;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ContractNetParticipantAction<A extends Agent<A, ?>> extends FiniteStateAction<A> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ContractNetParticipantAction.class);
    private static final int TIMEOUT_ACCEPT_STEPS = 1;
    private int timeoutCounter;
    private int nExpectedProposeAnswers;
    private MessageTemplate template = MessageTemplates.alwaysFalse();

    protected ContractNetParticipantAction(ContractNetParticipantAction<A> cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
        this.timeoutCounter = cloneable.timeoutCounter;
        this.nExpectedProposeAnswers = cloneable.nExpectedProposeAnswers;
        this.template = cloneable.template;
    }

    protected ContractNetParticipantAction(AbstractBuilder<A, ? extends ContractNetParticipantAction<A>,? extends AbstractBuilder<A,?,?>> builder) {
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
    protected void executeState(Object state) {

        if (State.CHECK_CFP == state) {
            prepareForCommunication();
            template = createCFPTemplate(getOntology());

            final List<ACLMessage<A>> cfpReplies = Lists.newArrayList();
            final Iterable<AgentMessage<A>> proposalCalls = agent().getMessages(template);
            for (ACLMessage<A> cfp : proposalCalls) {

                ACLMessage<A> cfpReply;
                try {
                    cfpReply = checkNotNull(handleCFP(cfp)).build();
                } catch (NotUnderstoodException e) {
                    cfpReply = ImmutableACLMessage.createReply(cfp, agent())
                            .performative(ACLPerformative.NOT_UNDERSTOOD)
                            .content(e.getMessage(), String.class).build();
                    LOGGER.warn("Message not understood {}", cfp, e);
                }
                checkCFPReply(cfpReply);
                cfpReplies.add(cfpReply);
                LOGGER.debug("{}: Replying to CFP with {}", this, cfpReply);
                agent().sendMessage(cfpReply);

                if (cfpReply.matches(MessageTemplates.performative(ACLPerformative.PROPOSE)))
                    ++nExpectedProposeAnswers;
            }

            if (nExpectedProposeAnswers > 0) {
                template = createProposalReplyTemplate(cfpReplies);
                timeoutCounter = 0;
                transition(State.WAIT_FOR_ACCEPT);
            }
            else
                endTransition(State.NO_CFP);
        }
        else if (State.WAIT_FOR_ACCEPT == state) {
            final Iterable<AgentMessage<A>> receivedMessages = agent().getMessages(getTemplate());
            for (ACLMessage<A> receivedMessage : receivedMessages) {
                switch (receivedMessage.getPerformative()) {
                    case ACCEPT_PROPOSAL:
                        ACLMessage<A> informMessage;
                        try {
                            informMessage = handleAccept(receivedMessage).build();
                        } catch (NotUnderstoodException e) {
                            informMessage = ImmutableACLMessage.createReply(receivedMessage, agent())
                                    .performative(ACLPerformative.NOT_UNDERSTOOD)
                                    .content(e.getMessage(), String.class).build();

                            LOGGER.warn("Message not understood {}", receivedMessage, e);
                        }
                        checkAcceptReply(informMessage);
                        LOGGER.debug("{}: Accepting proposal", this);
                        agent().sendMessage(informMessage);
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
        }
        else
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

    protected void prepareForCommunication() {}

    protected abstract ImmutableACLMessage.Builder<A> handleAccept(ACLMessage<A> message);

    @SuppressWarnings("UnusedParameters") // hook method
    protected void handleReject(ACLMessage<A> message) {}

    protected abstract ImmutableACLMessage.Builder<A> handleCFP(ACLMessage<A> message);

    private static MessageTemplate createCFPTemplate(final String ontology) {
        assert ontology != null;
        return MessageTemplates.and(
                MessageTemplates.ontology(ontology),
                MessageTemplates.performative(ACLPerformative.CFP)
        );
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends ContractNetParticipantAction, B extends AbstractBuilder<A, C, B>> extends FiniteStateAction.AbstractBuilder<A, C, B> implements Serializable {
        private int timeoutCounter;
        private int nExpectedProposeAnswers;
        private MessageTemplate template = MessageTemplates.alwaysFalse();

        protected AbstractBuilder() {}

        protected AbstractBuilder(ContractNetParticipantAction<A> action) {
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
