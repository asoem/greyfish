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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class ImmutableMarkovChainTest {

    @Test
    public void testSimpleChain() throws Exception {
        final ImmutableMarkovChain<String> chain = ImmutableMarkovChain.<String>builder()
                .put("A", "B", 1)
                .put("B", "C", 1)
                .build();

        final String initialState = "A";

        final String endState = chain.apply(chain.apply(chain.apply(initialState)));

        assertThat(endState, is("C"));
    }

    @Test
    public void testParse() throws Exception {
        // given
        final String rule = "A -> B : 1.0; B -> C : 1.0";

        // when
        final ImmutableMarkovChain<String> chain = ImmutableMarkovChain.parse(rule);

        // then
        final String endState = chain.apply(chain.apply("A"));
        assertThat(endState, is("C"));
    }
}
