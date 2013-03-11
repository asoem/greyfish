package org.asoem.greyfish.utils.concurrent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 03.04.12
 * Time: 12:50
 */
public class RecursiveActionsTest {
    @Test
    public void testForeach() throws Exception {
        // given
        final Runnable mock = mock(Runnable.class);
        final int listSize = 1000;
        final int threshold = 100;
        List<Runnable> list = ImmutableList.copyOf(Iterables.limit(Iterables.cycle(mock), listSize));
        VoidFunction<Runnable> fun = new VoidFunction<Runnable>() {
            @Override
            public void process(Runnable o) {
                o.run();
            }
        };
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        // when
        final RecursiveAction recursiveAction = RecursiveActions.foreach(list, fun, threshold);
        forkJoinPool.invoke(recursiveAction);

        // then
        verify(mock, times(listSize)).run();
    }
}
