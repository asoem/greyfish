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

import static com.google.common.base.Preconditions.checkState;

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

    public ContractNetInitiatorAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
        initFSM();
    }

    protected ContractNetInitiatorAction(ContractNetInitiatorAction cloneable, CloneMap cloneMap) {
        super(cloneable, cloneMap);
        initFSM();
    }

    private MessageTemplate getTemplate() {
        return template;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
    }

    private MessageTemplate template = MessageTemplate.alwaysFalse();
    private int nProposalsMax;

    private void initFSM() {
        registerInitialState(State.SEND_CFP, new StateAction() {

            @Override
            public Object run() {
                if (!canInitiate())
                   return State.NO_RECEIVERS;

                ACLMessage cfpMessage = createCFP().source(getComponentOwner().getId()).performative(ACLPerformative.CFP).build();
                sendMessage(cfpMessage);
                nProposalsMax = cfpMessage.getAllReceiver().size();
                timeoutCounter = 0;
                nProposalsReceived = 0;
                template = createCFPReplyTemplate(cfpMessage);

                return State.WAIT_FOR_PROPOSALS;
            }
        });

        registerIntermediateState(State.WAIT_FOR_PROPOSALS, new StateAction() {

            @Override
            public Object run() {

                Collection<ACLMessage> proposeReplies = Lists.newArrayList();
                for (ACLMessage receivedMessage : receiveMessages(getTemplate())) {
                    assert (receivedMessage != null);

                    ACLMessage proposeReply = null;
                    switch (receivedMessage.getPerformative()) {

                        case PROPOSE:
                            try {
                                proposeReply = handlePropose(receivedMessage).build();
                                assert (proposeReply != null);
                                proposeReplies.add(proposeReply);
                                ++nProposalsReceived;
                                LOGGER.trace("{}: Received proposal", ContractNetInitiatorAction.this);
                            } catch (NotUnderstoodException e) {
                                proposeReply = receivedMessage.createReplyFrom(getComponentOwner().getId())
                                        .performative(ACLPerformative.NOT_UNDERSTOOD)
                                        .stringContent(e.getMessage()).build();
                                LOGGER.debug("{}: Message not understood", ContractNetInitiatorAction.this, e);
                            } finally {
                                assert proposeReply != null;
                            }
                            checkProposeReply(proposeReply);
                            sendMessage(proposeReply);
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
                    return State.END;
                } else if (timeoutCounter > PROPOSAL_TIMEOUT_STEPS || nProposalsReceived == nProposalsMax) {
                    if (timeoutCounter > PROPOSAL_TIMEOUT_STEPS)
                        LOGGER.trace("{}: entered TIMEOUT for accepting proposals. Received {} proposals", ContractNetInitiatorAction.this, nProposalsReceived);

                    timeoutCounter = 0;

                    if (nProposalsReceived > 0) {
                        template = createAcceptReplyTemplate(proposeReplies);
                        nInformReceived = 0;
                        return State.WAIT_FOR_INFORM;
                    } else
                        return State.TIMEOUT;
                } else {
                    return State.WAIT_FOR_PROPOSALS;
                }
            }
        });

        registerIntermediateState(State.WAIT_FOR_INFORM, new StateAction() {

            @Override
            public Object run() {
                assert timeoutCounter == 0 && nInformReceived == 0 || timeoutCounter != 0;

                for (ACLMessage receivedMessage : receiveMessages(getTemplate())) {
                    assert receivedMessage != null;

                    switch (receivedMessage.getPerformative()) {

                        case INFORM:
                            try {
                                handleInform(receivedMessage);
                            } catch (NotUnderstoodException e) {
                                LOGGER.error("{}: HandleInform failed: ", ContractNetInitiatorAction.this, e);
                            }
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
                    return State.END;
                else if (timeoutCounter > INFORM_TIMEOUT_STEPS)
                    return State.TIMEOUT;
                else
                    return State.WAIT_FOR_INFORM;
            }
        });

        registerFailureState(State.TIMEOUT, new EndStateAction(State.TIMEOUT));
        registerFailureState(State.NO_RECEIVERS, new EndStateAction(State.NO_RECEIVERS));
        registerFailureState(State.END, new EndStateAction(State.END));
    }

    protected abstract boolean canInitiate();

    private static MessageTemplate createAcceptReplyTemplate(final Iterable<ACLMessage> acceptMessages) {
        if (Iterables.isEmpty(acceptMessages))
            return MessageTemplate.alwaysFalse();
        else
            return MessageTemplate.any( // is a reply
                    Iterables.toArray(
                            Iterables.transform(acceptMessages, new Function<ACLMessage, MessageTemplate>() {
                                @Override
                                public MessageTemplate apply(ACLMessage aclMessage) {
                                    return MessageTemplate.isReplyTo(aclMessage);
                                }
                            }),
                            MessageTemplate.class));
    }

    private static MessageTemplate createCFPReplyTemplate(final ACLMessage cfp) {
        return MessageTemplate.isReplyTo(cfp);
    }

    private static void checkProposeReply(ACLMessage response) {
        assert(response != null);
        assert(response.matches(MessageTemplate.any(
                MessageTemplate.performative(ACLPerformative.ACCEPT_PROPOSAL),
                MessageTemplate.performative(ACLPerformative.REJECT_PROPOSAL),
                MessageTemplate.performative(ACLPerformative.NOT_UNDERSTOOD))));
    }

    protected abstract ACLMessage.Builder createCFP();

    protected abstract ACLMessage.Builder handlePropose(ACLMessage message) throws NotUnderstoodException;

    protected void handleRefuse(ACLMessage message) {}

    protected void handleFailure(ACLMessage message) {}

    protected void handleInform(ACLMessage message) throws NotUnderstoodException {
    }

    protected abstract String getOntology();

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        checkState(!Strings.isNullOrEmpty(getOntology()));
    }
}