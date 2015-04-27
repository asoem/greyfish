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
import com.google.common.base.Optional;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.asoem.greyfish.core.test.GreyfishMatchers.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class GenericConditionTest {

    /*
    @Test
    public void testDeepClone() throws Exception {
        // given
        final AgentAction<Basic2DAgent> action = when(mock(AgentAction.class).deepClone(any(DeepCloner.class))).thenReturn(mock(AgentAction.class)).getMock();

        final ActionCondition<Basic2DAgent> condition = when(mock(ActionCondition.class).deepClone(any(DeepCloner.class))).thenReturn(mock(ActionCondition.class)).getMock();
        given(condition.getAction()).willReturn(Optional.of(action));

        final Callback<Object, Boolean> callback = Callbacks.constant(true);

        final GenericCondition<Basic2DAgent> genericCondition = GenericCondition.evaluate(callback);
        genericCondition.setParent(condition);

        // when
        final GenericCondition<Basic2DAgent> clone = CycleCloner.clone(genericCondition);

        // then
        assertThat(clone, isSameAs(genericCondition));
    }
    */

    @Test
    public void testSerialization() throws Exception {
        // given
        final GenericCondition<Basic2DAgent> genericCondition = GenericCondition.evaluate(Callbacks.constant(true));
        // when
        final GenericCondition<Basic2DAgent> copy = Persisters.copyAsync(genericCondition, Persisters.javaSerialization());
        // then
        assertThat(copy, isSameAs(genericCondition));
    }

    private static Matcher<? super GenericCondition<Basic2DAgent>> isSameAs(final GenericCondition<Basic2DAgent> genericCondition) {
        return Matchers.<GenericCondition<Basic2DAgent>>allOf(
                is(not(nullValue())),
                is(not(sameInstance(genericCondition))),
                has("callback == " + genericCondition.getCallback(), new Function<GenericCondition<Basic2DAgent>, Callback<? super GenericCondition<Basic2DAgent>, Boolean>>() {
                    @Override
                    public Callback<? super GenericCondition<Basic2DAgent>, Boolean> apply(final GenericCondition<Basic2DAgent> input) {
                        return input.getCallback();
                    }
                }, is(Matchers.<Callback<? super GenericCondition<Basic2DAgent>, Boolean>>equalTo(genericCondition.getCallback()))),
                has("action ~= " + genericCondition.getAction(), new Function<GenericCondition<Basic2DAgent>, Optional<AgentAction<?>>>() {
                    @Override
                    public Optional<AgentAction<?>> apply(final GenericCondition<Basic2DAgent> input) {
                        return input.getAction();
                    }
                }, is(instanceOf(Optional.class))),
                has("parent ~= " + genericCondition.getParent(), new Function<GenericCondition<Basic2DAgent>, ActionCondition<Basic2DAgent>>() {
                    @Override
                    public ActionCondition<Basic2DAgent> apply(final GenericCondition<Basic2DAgent> input) {
                        return input.getParent();
                    }
                }, is(genericCondition.getParent() == null ? nullValue() : instanceOf(ActionCondition.class)))
        );
    }
}
