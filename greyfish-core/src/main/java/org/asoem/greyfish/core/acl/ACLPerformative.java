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

package org.asoem.greyfish.core.acl;

public enum ACLPerformative {
    /**
     * The action of accepting a previously submitted {@link #PROPOSE propose} to an action.
     */
    ACCEPT_PROPOSAL,
    /**
     * The action of agreeing to perform a {@link #REQUEST requested} action made by another agent. Agent will carry it
     * out.
     */
    AGREE,
    /**
     * Agent wants to cancel a previous {@link #REQUEST request}.
     */
    CANCEL,
    /**
     * Agent issues a {@code call for proposals}. It contains the actions to be carried out and any other terms of the
     * agreement.
     */
    CFP,
    /**
     * The sender confirms to the receiver the truth of the content. The sender initially believed that the receiver was
     * unsure about it.
     */
    CONFIRM,
    /**
     * The sender confirms to the receiver the falsity of the content.
     */
    DISCONFIRM,
    /**
     * Tell the other agent that a previously {@link #REQUEST requested} action failed.
     */
    FAILURE,
    /**
     * Tell another agent something. The sender must believe in the truth of the statement. Most used performative.
     */
    INFORM,
    /**
     * Used as content of {@link #REQUEST request} to ask another agent to tell us is a statement is true or false.
     */
    INFORM_IF,
    /**
     * Like {@link #INFORM_IF} but asks for the value of the expression.
     */
    INFORM_REF,
    /**
     * Sent when the agent did not understand the message.
     */
    NOT_UNDERSTOOD,
    /**
     * Asks another agent so forward this same propagate message to others.
     */
    PROPAGATE,
    /**
     * Used as a response to a cfp. Agent proposes a deal.
     */
    PROPOSE,
    /**
     * The sender wants the receiver to select target agents denoted by a given description and to send an embedded
     * message to them.
     */
    PROXY,
    /**
     * The action of asking another agent whether or not a given proposition is true.
     */
    QUERY_IF,
    /**
     * The action of asking another agent for the object referred to by an referential expression.
     */
    QUERY_REF,
    /**
     * The action of refusing to perform a given action, and explaining the reason for the refusal.
     */
    REFUSE,
    /**
     * The action of rejecting a proposal to perform some action during a negotiation.
     */
    REJECT_PROPOSAL,
    /**
     * The sender requests the receiver to perform some action. Usually to request the receiver to perform another
     * communicative act.
     */
    REQUEST,
    /**
     * The sender wants the receiver to perform some action when some given proposition becomes true.
     */
    REQUEST_WHEN,
    /**
     * The sender wants the receiver to perform some action as soon as some proposition becomes true and thereafter each
     * time the proposition becomes true again.
     */
    REQUEST_WHENEVER,
    /**
     * The act of requesting a persistent intention to notify the sender of the value of a reference, and to notify
     * again whenever the object identified by the reference changes.
     */
    SUBSCRIBE,
    UNKNOWN
}
