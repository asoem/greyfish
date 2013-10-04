package org.asoem.greyfish.core.utils;

/**
 * A listener for time events.
 */
public interface DiscreteTimeListener {
    /**
     * Called when the time of the {@code source} has changed from {@code oldTime} to {@code newTime}.
     * @param source the source of the event
     * @param oldTime the old time
     * @param newTime the new time
     */
    void timeChanged(DiscreteTime source, long oldTime, long newTime);
}
