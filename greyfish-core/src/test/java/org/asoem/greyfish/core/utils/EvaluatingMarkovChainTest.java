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

package org.asoem.greyfish.core.utils;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.VanillaExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;


@RunWith(MockitoJUnitRunner.class)
public class EvaluatingMarkovChainTest {

    @Inject
    private VanillaExpressionFactory expressionFactory;

    public EvaluatingMarkovChainTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testSimpleChain() throws Exception {
        // given
        final EvaluatingMarkovChain<String> chain = EvaluatingMarkovChain.<String>builder(expressionFactory)
                .put("A", "B", "1.0")
                .put("B", "C", "1.0")
                .build();

        final String initialState = "A";

        // when
        final String endState = chain.apply(chain.apply(chain.apply(initialState)));

        // then
        MatcherAssert.assertThat(endState, is("C"));
    }

    @Test
    public void testParse() throws Exception {
        // given
        final String rule = "A -> B : 1.0; B -> C : 1.0";

        // when
        final EvaluatingMarkovChain<String> chain = EvaluatingMarkovChain.parse(rule, expressionFactory);

        // then
        final String endState = chain.apply(chain.apply("A"));
        MatcherAssert.assertThat(endState, is("C"));
    }

}
