package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;

import javax.annotation.Nullable;

public interface GFAction extends AgentComponent {

    /**
     * Check if all precondition are met for this action.
     * If so, the action will be in state {@link ActionState#PRECONDITIONS_MET} afterwards,
     * in {@link ActionState#PRECONDITIONS_FAILED} otherwise.
     *
     * @param simulation the context
     * @return {@code true}, if all preconditions are met
     */
    ActionState checkPreconditions(Simulation simulation);

    /**
     * Apply the action on it's agent in the given simulation context
     *
     * @param simulation the context of this action
     * @return the result of the application
     */
    public ActionState apply(Simulation simulation);

    /**
     * Reset this action, so that it will be in state {@link ActionState#INITIAL} afterwards.
     */
    void reset();

    /**
     * Set the condition set for this action
     *
     * @param rootCondition the condition set for this action
     */
    public void setCondition(@Nullable GFCondition rootCondition);

    /**
     * Get the condition set for this action
     *
     * @return the condition set for this action or {@code null}
     */
    @Nullable
    public GFCondition getCondition();

    /**
     * Evaluate the this action's condition.
     *
     * @param simulation the context of this action
     * @return {@code true} if this action's condition is {@code null} or evaluates
     *         ({@link GFCondition#apply(GFAction)}) to {@code true},
     *         {@code false} otherwise.
     */
    public boolean evaluateCondition(Simulation simulation);


    /**
     * @return the number of times this {@code GFAction} was executed when in
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
