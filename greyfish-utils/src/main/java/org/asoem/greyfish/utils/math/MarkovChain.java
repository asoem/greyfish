/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.math;

import java.util.Set;

/**
 * A markov chain defines a set of states and a function to transform any of these states into a following state.
 */
public interface MarkovChain<S> {

    /**
     * @return all registered states
     */
    Set<S> getStates();

    /**
     * Calculate the transition to the next state given the current {@code state}.
     *
     * @param state the current state
     * @return the next state
     */
    S apply(S state);
}
