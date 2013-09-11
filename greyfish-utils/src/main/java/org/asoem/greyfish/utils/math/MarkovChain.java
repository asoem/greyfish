package org.asoem.greyfish.utils.math;

import java.util.Set;

/**
 * A markov chain defines a set of states and a function to transform any of these states into a following state.
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
