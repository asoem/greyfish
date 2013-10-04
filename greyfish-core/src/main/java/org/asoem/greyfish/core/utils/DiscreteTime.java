package org.asoem.greyfish.core.utils;

/**
 * A discrete time without any unit.
 */
public interface DiscreteTime {

    /**
     * Add a listener to this time.
     * @param timeListener the listener to add
     */
    void addTimeChangeListener(DiscreteTimeListener timeListener);

    /**
     * Get the current time.
     * @return the current time
     */
    long getTime();
}
