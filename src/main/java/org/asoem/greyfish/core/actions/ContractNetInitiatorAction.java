package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ContractNetInitiatorAction extends FiniteStateAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractNetInitiatorAction.class);

    private static enum State {
        SEND_CFP,
        WAIT_FOR_PROPOSALS,
        WAIT_FOR_INFORM,
        END,
        NO_RECEIVERS, TIMEOUT
    }

    private static final int PROPOSAL_TIMEOUT_STEPS = 1;
    private static final int INFORM_TIMEOUT_STEPS = 1;

    private int timeoutCounter;
    private int nProposalsReceived;
    private int nInformReceived;

    public ContractNetInitiatorAction(AbstractBuilder<? extends ContractNetInitiatorAction, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected ContractNetInitiatorAction(ContractNetInitiatorAction cloneable, DeepCloner cloner) {
        super(cloneable, cloner);
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
    }

    private MessageTemplate template = MessageTemplates.alwaysFalse();
    private int nProposalsMax;

    @Override
    protected Object initialState() {
        return State.SEND_CFP;
    }

    @Override
    protected void executeState(Object state, Simulation simulation) {
        if (State.SEND_CFP.equals(state)) {
            if (!canInitiate(simulation)) {
                endTransition(State.NO_RECEIVERS);
            }
            else {
                ImmutableACLMessage<Agent> cfpMessage = createCFP()
                        .sender(agent())
                        .performative(ACLPerformative.CFP).build();

                simulation.deliverMessage(cfpMessage);

                nProposalsMax = cfpMessage.getRecipients().size();
                timeoutCounter = 0;
                nProposalsReceived = 0;
                template = createCFPReplyTemplate(cfpMessage);

                transition(State.WAIT_FOR_PROPOSALS);
            }
        }
        else if (State.WAIT_FOR_PROPOSALS.equals(state)) {
            Collection<ACLMessage> proposeReplies = Lists.newArrayList();
            for (ACLMessage<Agent> receivedMessage : agent().pullMessages(getTemplate())) {
                assert (receivedMessage != null);

                ACLMessage<Agent> proposeReply;
                switch (receivedMessage.getPerformative()) {

                    case PROPOSE:
                        try {
                            proposeReply = checkNotNull(handlePropose(receivedMessage)).build();
                            proposeReplies.add(proposeReply);
                            ++nProposalsReceived;
                            LOGGER.trace("{}: Received proposal", ContractNetInitiatorAction.this);
                        } catch (NotUnderstoodException e) {
                            proposeReply = ImmutableACLMessage.createReply(receivedMessage, agent())
                                    .performative(ACLPerformative.NOT_UNDERSTOOD)
                                    .content(e.getMessage(), String.class).build();
                            LOGGER.debug("{}: Message not understood", ContractNetInitiatorAction.this, e);
                        }
                        checkProposeReply(proposeReply);

                        simulation.deliverMessage(proposeReply);
                        break;

                    case REFUSE:
                        LOGGER.debug("{}: CFP was refused: ", ContractNetInitiatorAction.this, receivedMessage);
                        handleRefuse(receivedMessage);
                        --nProposalsMax;
                        break;

                    case NOT_UNDERSTOOD:
                        LOGGER.debug("{}: Communication Error: NOT_UNDERSTOOD received", ContractNetInitiatorAction.this);
                        --nProposalsMax;
                        break;

                    default:
                        LOGGER.debug("{}: Protocol Error: Expected performative PROPOSE, REFUSE or NOT_UNDERSTOOD, received {}.", ContractNetInitiatorAction.this, receivedMessage.getPerformative());
                        --nProposalsMax;
                        break;
                }
            }

            ++timeoutCounter;

            assert nProposalsMax >= 0;

            if (nProposalsMax == 0) {
                LOGGER.debug("{}: received 0 proposals for {} CFP messages", ContractNetInitiatorAction.this, nProposalsMax);
                endTransition(State.END);
            }
            else if (nProposalsReceived == nProposalsMax) {
                template = createAcceptReplyTemplate(proposeReplies);
                nInformReceived = 0;
                timeoutCounter = 0;
                transition(State.WAIT_FOR_INFORM);
            }
            else if (timeoutCounter > PROPOSAL_TIMEOUT_STEPS) {
                LOGGER.trace("{}: entered ACCEPT_TIMEOUT for accepting proposals. Received {} proposals", ContractNetInitiatorAction.this, nProposalsReceived);

                timeoutCounter = 0;
                failure("Timeout for INFORM Messages");
            }

            // else: No transition
        }
        else if (State.WAIT_FOR_INFORM.equals(state)) {
            assert timeoutCounter == 0 && nInformReceived == 0 || timeoutCounter != 0;

            for (ACLMessage<Agent> receivedMessage : agent().pullMessages(getTemplate())) {
                assert receivedMessage != null;

                switch (receivedMessage.getPerformative()) {

                    case INFORM:
                        handleInform(receivedMessage);
                        break;

                    case FAILURE:
                        LOGGER.debug("{}: Received FAILURE: {}", ContractNetInitiatorAction.this, receivedMessage);
                        handleFailure(receivedMessage);
                        break;

                    case NOT_UNDERSTOOD:
                        LOGGER.debug("{}: Received NOT_UNDERSTOOD: {}", ContractNetInitiatorAction.this, receivedMessage);
                        break;

                    default:
                        LOGGER.debug("{}: Expected none of INFORM, FAILURE or NOT_UNDERSTOOD: {}", ContractNetInitiatorAction.this, receivedMessage);
                        break;
                }

                ++nInformReceived;
            }

            ++timeoutCounter;

            if (nInformReceived == nProposalsReceived)
                endTransition(State.END);
            else if (timeoutCounter > INFORM_TIMEOUT_STEPS)
                failure("Timeout for INFORM Messages");
        }
    }

    protected abstract boolean canInitiate(Simulation simulation);

    private static MessageTemplate createAcceptReplyTemplate(final Iterable<? extends ACLMessage> acceptMessages) {
        if (Iterables.isEmpty(acceptMessages))
            return MessageTemplates.alwaysFalse();
        else
            return MessageTemplates.or( // is a reply
                    Iterables.toArray(
                            Iterables.transform(acceptMessages, new Function<ACLMessage, MessageTemplate>() {
                                @Override
                                public MessageTemplate apply(ACLMessage aclMessage) {
                                    return MessageTemplates.isReplyTo(aclMessage);
                                }
                            }),
                            MessageTemplate.class));
    }

    private static MessageTemplate createCFPReplyTemplate(final ImmutableACLMessage<Agent> cfp) {
        return MessageTemplates.isReplyTo(cfp);
    }

    private static void checkProposeReply(ACLMessage response) {
        assert(response != null);
        assert(response.matches(MessageTemplates.or(
                MessageTemplates.performative(ACLPerformative.ACCEPT_PROPOSAL),
                MessageTemplates.performative(ACLPerformative.REJECT_PROPOSAL),
                MessageTemplates.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    protected abstract ImmutableACLMessage.Builder<Agent> createCFP();

    protected abstract ImmutableACLMessage.Builder<Agent> handlePropose(ACLMessage<Agent> message) throws NotUnderstoodException;

    protected void handleRefuse(ACLMessage<Agent> message) {}

    protected void handleFailure(ACLMessage<Agent> message) {}

    protected void handleInform(ACLMessage<Agent> message) {
    }

    protected abstract String getOntology();

}