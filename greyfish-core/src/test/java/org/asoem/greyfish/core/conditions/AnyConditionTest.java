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

package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.asoem.greyfish.core.test.GreyfishMatchers.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;


public class AnyConditionTest {

    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition<Basic2DAgent> condition = mock(ActionCondition.class, withSettings().serializable());
        final AnyCondition<Basic2DAgent> anyCondition = AnyCondition.evaluates(condition);

        // when
        final AnyCondition<Basic2DAgent> copy = Persisters.copyAsync(anyCondition, Persisters.javaSerialization());

        // then
        assertThat(copy, isCopyOf(anyCondition));
    }

    private static <A extends Agent<?>> Matcher<AnyCondition<A>> isCopyOf(final AnyCondition<A> allCondition) {
        return allOf(
                is(not(sameInstance(allCondition))),
                has("equal child conditions",
                        new Function<AnyCondition<A>, List<ActionCondition<A>>>() {
                            @Override
                            public List<ActionCondition<A>> apply(final AnyCondition<A> input) {
                                return input.getChildConditions();
                            }
                        },
                        Matchers.<List<ActionCondition<A>>>allOf(
                                hasSize(allCondition.getChildConditions().size()),
                                everyItem(is(Matchers.<ActionCondition<A>>instanceOf(ActionCondition.class))),
                                everyItem(not(isIn(allCondition.getChildConditions()))))));
    }
}
