package org.asoem.greyfish.lang;

import org.asoem.greyfish.utils.ConfigurationHandler;

/**
 * User: christoph
 * Date: 24.09.11
 * Time: 09:37
 */
public interface Configurable {
    /**
     * Ask the object to add all members it wishes to get configured externally
     * @param e the handler implementation
     */
    public void configure(ConfigurationHandler e);
}
