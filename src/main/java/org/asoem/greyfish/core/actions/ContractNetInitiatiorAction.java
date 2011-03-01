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

import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.io.GreyfishLogger.GFACTIONS_LOGGER;

public abstract class ContractNetInitiatiorAction extends FiniteStateAction {

    private static final String SEND_CFP = "Send-cfp";
    private static final String WAIT_FOR_POROPOSALS = "Wait-for-proposals";
    private static final String WAIT_FOR_INFORM = "Wait-for-inform";
    private static final String END = "End";
    private static final String TIMEOUT = "Timeout";

    private static final int PROPOSAL_TIMEOUT = 2;
    private static final int INFORM_TIMEOUT = 1;

    private int timeoutCounter;
    private int nReceivedProposals;
    private int nReceivedAcceptAnswers;

    public ContractNetInitiatiorAction(AbstractGFAction.AbstractBuilder<?> builder) {
        super(builder);
        initFSM();
    }

    protected ContractNetInitiatiorAction(ContractNetInitiatiorAction cloneable, CloneMap cloneMap) {
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
    private int nProposalsExpected;

    private void initFSM() {
        registerInitialFSMState(SEND_CFP, new StateAction() {

            @Override
            public String action() {
                ACLMessage cfpMessage = createCFP().source(getComponentOwner().getId()).performative(ACLPerformative.CFP).build();
                sendMessage(cfpMessage);
                nProposalsExpected = cfpMessage.getAllReceiver().size();
                timeoutCounter = 0;
                nReceivedProposals = 0;
                template = createCFPReplyTemplate(cfpMessage);

                return WAIT_FOR_POROPOSALS;
            }
        });

        registerFSMState(WAIT_FOR_POROPOSALS, new StateAction() {

            @Override
            public String action() {

                Collection<ACLMessage> proposeReplies = Lists.newArrayList();
                for (ACLMessage receivedMessage : receiveMessages(getTemplate())) {

                    ACLMessage proposeReply = null;
                    switch (receivedMessage.getPerformative()) {

                        case PROPOSE:
                            try {
                                proposeReply = handlePropose(receivedMessage).build();
                                proposeReplies.add(proposeReply);
                                ++nReceivedProposals;
                            } catch (NotUnderstoodException e) {
                                proposeReply = receivedMessage.replyFrom(getComponentOwner().getId())
                                        .performative(ACLPerformative.NOT_UNDERSTOOD)
                                        .stringContent(e.getMessage()).build();
                                if (GFACTIONS_LOGGER.hasDebugEnabled())
                                    GFACTIONS_LOGGER.debug("ContractNetInit '" + this + "' Message not understood", e);
                            } finally {
                                assert proposeReply != null;
                            }
                            checkProposeReply(proposeReply);
                            sendMessage(proposeReply);
                            break;

                        case REFUSE:
                            if (GFACTIONS_LOGGER.hasDebugEnabled())
                                GFACTIONS_LOGGER.debug("ContractNetInit '" + this + "' CFP was refused: " + receivedMessage);
                            handleRefuse(receivedMessage);
                            --nProposalsExpected;
                            break;

                        case NOT_UNDERSTOOD:
                            if (GFACTIONS_LOGGER.hasDebugEnabled())
                                GFACTIONS_LOGGER.debug("ContractNetInit '" + this + "' Communication Error: NOT_UNDERSTOOD received");
                            --nProposalsExpected;
                            break;

                        default:
                            if (GFACTIONS_LOGGER.hasDebugEnabled()) GFACTIONS_LOGGER.debug("Protocol Error: " +
                                    "Expected PROPOSE, REFUSE or NOT_UNDERSTOOD," +
                                    "received " + receivedMessage.getPerformative());
                            --nProposalsExpected;
                            break;

                    }
                }
                ++timeoutCounter;


                assert nProposalsExpected >= 0;

                if (nProposalsExpected == 0) {
                    if (GFACTIONS_LOGGER.hasDebugEnabled())
                        GFACTIONS_LOGGER.debug("ContractNetInit '" + this + "' received 0 proposals for " + nProposalsExpected + "CFP messages");
                    return END;
                }
                else if (timeoutCounter == PROPOSAL_TIMEOUT || nReceivedProposals == nProposalsExpected) {
                    if (GFACTIONS_LOGGER.hasTraceEnabled() && timeoutCounter == PROPOSAL_TIMEOUT)
                        GFACTIONS_LOGGER.trace("ContractNetInit '" + this + "' entered TIMEOUT for proposals");
                    timeoutCounter = 0;
                    template = createAcceptReplyTemplate(proposeReplies);
                    return WAIT_FOR_INFORM;
                }
                else {
                    return WAIT_FOR_POROPOSALS;
                }
            }
        });

        registerFSMState(WAIT_FOR_INFORM, new StateAction() {

            @Override
            public String action() {
                for (ACLMessage receivedMessage : receiveMessages(getTemplate())) {
                    switch (receivedMessage.getPerformative()) {

                        case INFORM:
                            handleInform(receivedMessage);
                            break;

                        case FAILURE:
                            if (GFACTIONS_LOGGER.hasDebugEnabled())
                                GFACTIONS_LOGGER.debug("ContractNetInit '" + this + "' received FAILURE: " + receivedMessage);
                            handleFailure(receivedMessage);
                            break;

                        case NOT_UNDERSTOOD:
                            if (GFACTIONS_LOGGER.hasDebugEnabled())
                                GFACTIONS_LOGGER.debug("ContractNetInit '" + this + "' received NOT_UNDERSTOOD: " + receivedMessage);
                            break;

                        default:
                            if (GFACTIONS_LOGGER.hasDebugEnabled())
                                GFACTIONS_LOGGER.debug("ContractNetInit '" + this + "' expected none of INFORM, FAILURE or NOT_UNDERSTOOD:" + receivedMessage);
                            break;
                    }

                    ++nReceivedAcceptAnswers;
                }
                ++timeoutCounter;
                if (nReceivedAcceptAnswers == nReceivedProposals)
                    return END;
                else if (timeoutCounter == INFORM_TIMEOUT)
                    return TIMEOUT;
                else
                    return WAIT_FOR_INFORM;
            }
        });

        registerEndFSMState(TIMEOUT, new StateAction() {

            @Override
            public String action() {
                if (GFACTIONS_LOGGER.hasDebugEnabled())
                    GFACTIONS_LOGGER.debug(ContractNetInitiatiorAction.class.getSimpleName() + ": Timeout");
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

    protected void handleRefuse(ACLMessage message) {
    }

    protected void handleFailure(ACLMessage message) {
    }

    protected void handleInform(ACLMessage message) {
    }

    protected abstract String getOntology();

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        checkState(!Strings.isNullOrEmpty(getOntology()));
    }
}