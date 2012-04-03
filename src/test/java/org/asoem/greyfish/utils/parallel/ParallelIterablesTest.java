package org.asoem.greyfish.utils.parallel;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.base.VoidFunction;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 03.04.12
 * Time: 12:50
 */
public class ParallelIterablesTest {
    @Test
    public void testApply() throws Exception {
        // given
        final Object mock = mock(Object.class);
        Iterable<Object> iterable = Iterables.limit(Iterables.cycle(mock), 100);
        VoidFunction<Object> fun = new VoidFunction<Object>() {
            @Override
            public void apply(@Nullable Object o) {
                o.hashCode();
            }
        };

        // when
        ParallelIterables.apply(iterable, fun, 1000);

        // then
        verify(mock, times(1000)).hashCode();
    }
}
