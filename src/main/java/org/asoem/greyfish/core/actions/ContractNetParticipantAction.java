package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentMessage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ContractNetParticipantAction extends FiniteStateAction {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ContractNetParticipantAction.class);

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

    public ContractNetParticipantAction(AbstractActionBuilder<?,?> builder) {
        super(builder);
    }

    @Override
    protected Object initialState() {
        return State.CHECK_CFP;
    }

    @Override
    protected void executeState(Object state, Simulation simulation) {

        if (State.CHECK_CFP == state) {
            prepareForCommunication();
            template = createCFPTemplate(getOntology());

            final List<ACLMessage<Agent>> cfpReplies = Lists.newArrayList();
            final Iterable<AgentMessage> proposalCalls = agent().getMessages(template);
            for (ACLMessage<Agent> cfp : proposalCalls) {

                ACLMessage<Agent> cfpReply;
                try {
                    cfpReply = checkNotNull(handleCFP(cfp, simulation)).build();
                } catch (NotUnderstoodException e) {
                    cfpReply = ImmutableACLMessage.createReply(cfp, agent())
                            .performative(ACLPerformative.NOT_UNDERSTOOD)
                            .content(e.getMessage(), String.class).build();
                    LOGGER.warn("Message not understood {}", cfp, e);
                }
                checkCFPReply(cfpReply);
                cfpReplies.add(cfpReply);
                LOGGER.debug("{}: Replying to CFP with {}", this, cfpReply);
                simulation.deliverMessage(cfpReply);

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
            final Iterable<AgentMessage> receivedMessages = agent().getMessages(getTemplate());
            for (ACLMessage<Agent> receivedMessage : receivedMessages) {
                switch (receivedMessage.getPerformative()) {
                    case ACCEPT_PROPOSAL:
                        ACLMessage<Agent> informMessage;
                        try {
                            informMessage = handleAccept(receivedMessage, simulation).build();
                        } catch (NotUnderstoodException e) {
                            informMessage = ImmutableACLMessage.createReply(receivedMessage, agent())
                                    .performative(ACLPerformative.NOT_UNDERSTOOD)
                                    .content(e.getMessage(), String.class).build();

                            LOGGER.warn("Message not understood {}", receivedMessage, e);
                        }
                        checkAcceptReply(informMessage);
                        LOGGER.debug("{}: Accepting proposal", this);
                        simulation.deliverMessage(informMessage);
                        break;
                    case REJECT_PROPOSAL:
                        handleReject(receivedMessage, simulation);
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

    protected abstract ImmutableACLMessage.Builder<Agent> handleAccept(ACLMessage<Agent> message, Simulation simulation);

    @SuppressWarnings("UnusedParameters") // hook method
    protected void handleReject(ACLMessage<Agent> message, Simulation simulation) {}

    protected abstract ImmutableACLMessage.Builder<Agent> handleCFP(ACLMessage<Agent> message, Simulation simulation);

    private static MessageTemplate createCFPTemplate(final String ontology) {
        assert ontology != null;
        return MessageTemplates.and(
                MessageTemplates.ontology(ontology),
                MessageTemplates.performative(ACLPerformative.CFP)
        );
    }

}
