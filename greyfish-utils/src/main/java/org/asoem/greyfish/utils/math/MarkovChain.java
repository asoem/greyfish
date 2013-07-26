package org.asoem.greyfish.utils.math;

import java.util.Set;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 12:17
 */
public interface MarkovChain<S> {

    /**
     *
     * @return all registered states
     */
    Set<S> getStates();

    /**
     * Make a transition to the next state as defined by the markovMatrix
     *
     * @param state the current state
     * @return the next state
     */
    S apply(S state);
}
