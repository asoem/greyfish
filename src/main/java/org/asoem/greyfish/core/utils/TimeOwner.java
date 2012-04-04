package org.asoem.greyfish.core.utils;

/**
 * User: christoph
 * Date: 01.02.12
 * Time: 13:59
 */
public interface TimeOwner {
    void addTimeListener(TimeListener timeListener);

    int getCurrentStep();
}
