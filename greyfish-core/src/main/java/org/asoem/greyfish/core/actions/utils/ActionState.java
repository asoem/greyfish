package org.asoem.greyfish.core.actions.utils;

public enum ActionState {
    /**
     * Initial.
     */
    INITIAL,
    /**
     * All preconditions for this actions have been met. Internal usage only.
     */
    PRECONDITIONS_MET,
    /**
     * A precondition for this actions has not been met. Internal usage only.
     */
    PRECONDITIONS_FAILED,
    /**
     * Action is not finished yet
     */
    INTERMEDIATE,
    /**
     * Action is finished
     */
    COMPLETED,
    /**
     * Action execution was aborted
     */
    ABORTED
}
