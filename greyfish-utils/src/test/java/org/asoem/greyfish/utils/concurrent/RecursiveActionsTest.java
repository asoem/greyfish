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

package org.asoem.greyfish.utils.concurrent;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;


public class RecursiveActionsTest {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    @Test
    public void testForeach() throws Exception {
        // given
        final Runnable mock = mock(Runnable.class);
        final int listSize = 1000;
        final int threshold = 100;
        final List<Runnable> list = ImmutableList.copyOf(Iterables.limit(Iterables.cycle(mock), listSize));
        final VoidFunction<Runnable> fun = new VoidFunction<Runnable>() {
            @Override
            public void process(final Runnable o) {
                o.run();
            }
        };

        // when
        final RecursiveAction recursiveAction = RecursiveActions.foreach(list, fun, threshold);
        forkJoinPool.invoke(recursiveAction);

        // then
        verify(mock, times(listSize)).run();
    }

    @Test
    public void testTransform() throws Exception {
        // given
        final List<Integer> ints = Lists.newArrayList();
        for (int i = 0; i < 1000; i++) {
            ints.add(i);
        }


        // when
        final RecursiveActions.Transform<Integer, Integer> transform =
                new RecursiveActions.Transform<Integer, Integer>(ints, 0, ints.size(), 10, Functions.<Integer>identity());
        final List<Integer> invoke = ImmutableList.copyOf(forkJoinPool.invoke(transform));

        // then
        assertThat(invoke, is(equalTo(ints)));
    }
}
