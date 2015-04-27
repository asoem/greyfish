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

package org.asoem.greyfish.utils.base;

import com.google.common.base.Supplier;
import org.junit.Test;

import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
        cache.get();
        checkState(!cache.isInvalid());

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

        // when
        cache.get();

        // then
        assertThat(cache.isInvalid(), is(false));
    }

    @Test
    public void testUpdateWhenValid() throws Exception {
        // given
        final Supplier<Integer> supplier = mock(Supplier.class);
        given(supplier.get()).willReturn(42);
        final SingleElementCache<Integer> cache = SingleElementCache.memoize(supplier);
        cache.get();

        // when
        cache.get();

        // then
        verify(supplier, times(1)).get();
    }

    @Test
    public void testUpdateWhenNotValid() throws Exception {
        // given
        final Supplier<Integer> supplier = mock(Supplier.class);
        given(supplier.get()).willReturn(42);
        final SingleElementCache<Integer> cache = SingleElementCache.memoize(supplier);
        cache.get();
        cache.invalidate();

        // when
        cache.get();

        // then
        verify(supplier, times(2)).get();
    }
}
