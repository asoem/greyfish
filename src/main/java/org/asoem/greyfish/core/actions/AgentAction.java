package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.conditions.ActionCondition;

import javax.annotation.Nullable;

public interface AgentAction<A extends Agent<A, ?, ?>> extends AgentComponent<A> {

    /**
     * Check if all precondition are met for this action.
     * If so, the action will be in state {@link ActionState#PRECONDITIONS_MET} afterwards,
     * in {@link ActionState#PRECONDITIONS_FAILED} otherwise.
     *
     * @return {@code true}, if all preconditions are met
     */
    ActionState checkPreconditions();

    /**
     * Apply the action on it's agent in the given simulation context
     *
     * @return the result of the application
     */
    public ActionState apply();

    /**
     * Reset this action, so that it will be in state {@link ActionState#INITIAL} afterwards.
     */
    void reset();

    /**
     * Set the condition set for this action
     *
     * @param rootCondition the condition set for this action
     */
    public void setCondition(@Nullable ActionCondition<A> rootCondition);

    /**
     * Get the condition set for this action
     *
     * @return the condition set for this action or {@code null}
     */
    @Nullable
    public ActionCondition<A> getCondition();

    /**
     * Evaluate the this action's condition.
     *
     * @return {@code true} if this action's condition is {@code null} or evaluates
     *         ({@link org.asoem.greyfish.core.conditions.ActionCondition#evaluate()}) to {@code true},
     *         {@code false} otherwise.
     */
    public boolean evaluateCondition();


    /**
     * @return the number of times this {@code AgentAction} was executed when in
     *         {@link org.asoem.greyfish.core.actions.utils.ActionState#INITIAL}
     */
    public int getCompletionCount();

    /**
     *
     * @param steps
     * @return
     */
    public boolean wasNotExecutedForAtLeast(final int steps);

    /**
     * @return
     */
    int lastCompletionStep();

    ActionState getState();
}
