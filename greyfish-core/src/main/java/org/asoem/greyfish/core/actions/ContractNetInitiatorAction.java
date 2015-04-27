/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.*;
import org.asoem.greyfish.core.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ContractNetInitiatorAction<A extends Agent<?>> extends FiniteStateAction<A> {

    private static final Logger logger = LoggerFactory.getLogger(ContractNetInitiatorAction.class);
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
    protected final void executeState(final Object state, final AgentContext<A> context) {
        if (State.SEND_CFP.equals(state)) {
            if (!canInitiate(context)) {
                endTransition(State.NO_RECEIVERS);
            } else {
                final ImmutableACLMessage<A> cfpMessage = createCFP(context)
                        .sender(context.agent())
                        .performative(ACLPerformative.CFP).build();
                logger.debug("{}: Calling for proposals", this, cfpMessage);
                context.sendMessage(cfpMessage);

                nProposalsMax = cfpMessage.getRecipients().size();
                timeoutCounter = 0;
                nProposalsReceived = 0;
                template = createCFPReplyTemplate(cfpMessage);

                transition(State.WAIT_FOR_PROPOSALS);
            }
        } else if (State.WAIT_FOR_PROPOSALS.equals(state)) {
            final Collection<ACLMessage<A>> proposeReplies = Lists.newArrayList();
            for (final ACLMessage<A> receivedMessage : context.getMessages(getTemplate())) {
                assert (receivedMessage != null);

                ACLMessage<A> proposeReply;
                switch (receivedMessage.getPerformative()) {

                    case PROPOSE:
                        try {
                            proposeReply = checkNotNull(handlePropose(receivedMessage, context)).build();
                            proposeReplies.add(proposeReply);
                            ++nProposalsReceived;
                            logger.debug("{}: Received proposal", this, receivedMessage);
                        } catch (NotUnderstoodException e) {
                            proposeReply = ImmutableACLMessage.createReply(receivedMessage, context.agent())
                                    .performative(ACLPerformative.NOT_UNDERSTOOD)
                                    .content(e.getMessage()).build();
                            logger.warn("Message not understood {}", receivedMessage, e);
                        }
                        checkProposeReply(proposeReply);
                        logger.debug("{}: Replying to proposal", this, proposeReply);
                        context.sendMessage(proposeReply);
                        break;

                    case REFUSE:
                        logger.debug("{}: CFP was refused: ", this, receivedMessage);
                        handleRefuse(receivedMessage, context);
                        --nProposalsMax;
                        break;

                    case NOT_UNDERSTOOD:
                        logger.debug("{}: Communication Error: NOT_UNDERSTOOD received", this);
                        --nProposalsMax;
                        break;

                    default:
                        logger.debug("{}: Protocol Error:"
                                + "Expected performative PROPOSE, REFUSE or NOT_UNDERSTOOD, received {}.",
                                this, receivedMessage.getPerformative());
                        --nProposalsMax;
                        break;
                }
            }

            ++timeoutCounter;

            assert nProposalsMax >= 0;

            if (nProposalsMax == 0) {
                logger.debug("{}: received 0 proposals for {} CFP messages", this, nProposalsMax);
                endTransition(State.END);
            } else if (nProposalsReceived == nProposalsMax) {
                template = createAcceptReplyTemplate(proposeReplies);
                nInformReceived = 0;
                timeoutCounter = 0;
                transition(State.WAIT_FOR_INFORM);
            } else if (timeoutCounter > PROPOSAL_TIMEOUT_STEPS) {
                logger.trace("{}: entered ACCEPT_TIMEOUT for accepting proposals. Received {} proposals",
                        this, nProposalsReceived);

                timeoutCounter = 0;
                failure("Timeout for INFORM Messages");
            }

            // else: No transition
        } else if (State.WAIT_FOR_INFORM.equals(state)) {
            assert timeoutCounter == 0 && nInformReceived == 0 || timeoutCounter != 0;

            for (final ACLMessage<A> receivedMessage : context.getMessages(getTemplate())) {
                assert receivedMessage != null;

                switch (receivedMessage.getPerformative()) {

                    case INFORM:
                        handleInform(receivedMessage, context);
                        break;

                    case FAILURE:
                        logger.debug("{}: Received FAILURE: {}", this, receivedMessage);
                        handleFailure(receivedMessage, context);
                        break;

                    case NOT_UNDERSTOOD:
                        logger.debug("{}: Received NOT_UNDERSTOOD: {}", this, receivedMessage);
                        break;

                    default:
                        logger.debug("{}: Expected none of INFORM, FAILURE or NOT_UNDERSTOOD: {}",
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

    protected abstract boolean canInitiate(final AgentContext<A> context);

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

    private static <A extends Agent<?>> MessageTemplate createCFPReplyTemplate(final ACLMessage<A> cfp) {
        return MessageTemplates.isReplyTo(cfp);
    }

    private static void checkProposeReply(final ACLMessage response) {
        assert response != null;
        assert response.matches(MessageTemplates.or(
                MessageTemplates.performative(ACLPerformative.ACCEPT_PROPOSAL),
                MessageTemplates.performative(ACLPerformative.REJECT_PROPOSAL),
                MessageTemplates.performative(ACLPerformative.NOT_UNDERSTOOD)));
    }

    protected abstract ImmutableACLMessage.Builder<A> createCFP(final AgentContext<A> context);

    protected abstract ImmutableACLMessage.Builder<A> handlePropose(ACLMessage<A> message, final AgentContext<A> context);

    @SuppressWarnings("UnusedParameters") // hook method
    protected void handleRefuse(final ACLMessage<A> message, final AgentContext<A> context) {
    }

    @SuppressWarnings("UnusedParameters") // hook method
    protected void handleFailure(final ACLMessage<A> message, final AgentContext<A> context) {
    }

    protected void handleInform(final ACLMessage<A> message, final AgentContext<A> context) {
    }

    protected abstract String getOntology();

    protected abstract static class AbstractBuilder<A extends Agent<?>,
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
