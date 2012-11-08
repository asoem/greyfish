package org.asoem.greyfish.core.actions.utils;

/**
 * User: christoph
 * Date: 10.10.11
 * Time: 10:29
 */
public enum ActionState {
    INITIAL,
    PRECONDITIONS_MET,
    INTERMEDIATE,
    COMPLETED,
    ABORTED,
    PRECONDITIONS_FAILED,
    INSUFFICIENT_ENERGY
}
