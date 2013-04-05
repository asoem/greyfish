package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * User: christoph
 * Date: 05.04.13
 * Time: 14:05
 */
public class SingleElementCacheTest {
    @Test
    public void testGet() throws Exception {
        // given
        final Supplier<Integer> supplier = mock(Supplier.class);
        given(supplier.get()).willReturn(42);
        final SingleElementCache<Integer> cache = SingleElementCache.memoize(supplier);

        // when
        final Integer integer = cache.get();

        // then
        assertThat(integer, is(42));
    }

    @Test
    public void testInvalidate() throws Exception {
        // given
        final Supplier<Integer> supplier = mock(Supplier.class);
        given(supplier.get()).willReturn(42);
        final SingleElementCache<Integer> cache = SingleElementCache.memoize(supplier);

        // when
        cache.invalidate();

        // then
        assertThat(cache.isInvalid(), is(true));
    }

    @Test
    public void testUpdate() throws Exception {
        // given
        final Supplier<Integer> supplier = mock(Supplier.class);
        final SingleElementCache<Integer> cache = SingleElementCache.memoize(supplier);
        cache.invalidate();

        // when
        cache.update();

        // then
        assertThat(cache.isInvalid(), is(false));
    }

    @Test
    public void testUpdateWhenValid() throws Exception {
        // given
        final Supplier<Integer> supplier = mock(Supplier.class);
        given(supplier.get()).willReturn(42);
        final SingleElementCache<Integer> cache = SingleElementCache.memoize(supplier);
        cache.update();

        // when
        cache.update();

        // then
        verify(supplier).get();
    }

    @Test
    public void testAsynchronousInvalidationDuringUpdate() throws Exception {
        // given
        final Supplier<Integer> supplier = mock(Supplier.class);
        given(supplier.get()).willAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(10);
                return 42;
            }
        });
        final SingleElementCache<Integer> cache = SingleElementCache.memoize(supplier);

        // when
        final Future<?> future = Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                cache.update();
            }
        });
        Thread.sleep(2);
        cache.invalidate();
        future.get();

        // then
        verify(supplier, times(2)).get();
    }
}
