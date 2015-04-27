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

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class AllConditionTest {

    /*
    @Test
    public void testDeepClone() throws Exception {
        // given
        final ActionCondition<Basic2DAgent> childMock = mock(ActionCondition.class);
        when(childMock.deepClone(any(DeepCloner.class))).thenReturn(mock(ActionCondition.class));
        final AllCondition<Basic2DAgent> allCondition = AllCondition.evaluates(childMock, childMock);

        // when
        final AllCondition<Basic2DAgent> clone = CycleCloner.clone(allCondition);

        // then
        assertThat(clone.getChildConditions(), hasSize(allCondition.getChildConditions().size()));
        verify(childMock, times(2)).deepClone(any(DeepCloner.class));
    }
    */
    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition<Basic2DAgent> condition = mock(ActionCondition.class, withSettings().serializable());
        final AllCondition<Basic2DAgent> allCondition = AllCondition.evaluates(condition, condition);

        // when
        final AllCondition<Basic2DAgent> copy = Persisters.copyAsync(allCondition, Persisters.javaSerialization());

        // then
        assertThat(copy.getChildConditions(), hasSize(allCondition.getChildConditions().size()));
    }
}
