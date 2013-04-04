package org.asoem.greyfish.core.utils;

/**
 * User: christoph
 * Date: 01.02.12
 * Time: 14:11
 */
public interface TimeListener {
    void timeProceeded(TimeOwner source, int oldTime, int newTime);
}
