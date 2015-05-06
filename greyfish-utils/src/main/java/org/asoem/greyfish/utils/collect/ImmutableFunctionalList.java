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

package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ImmutableFunctionalList<E> extends AbstractFunctionalList<E> {

    @SuppressWarnings("unchecked")
    public static <E> FunctionalList<E> copyOf(final List<? extends E> list) {
        checkNotNull(list);
        if (list instanceof ImmutableFunctionalList) {
            return (FunctionalList<E>) list;
        }

        final int size = list.size();
        switch (size) {
            case 0:
                return of();
            case 1:
                return new UnrolledList1<>(list.get(0));
            case 2:
                return new UnrolledList2<>(list);
            case 3:
                return new UnrolledList3<>(list);
            case 4:
                return new UnrolledList4<>(list);
            case 5:
                return new UnrolledList5<>(list);
            case 6:
                return new UnrolledList6<>(list);
            case 7:
                return new UnrolledList7<>(list);
            default:
                return new ImmutableFunctionalListN<>(list);
        }
    }

    public static <E> FunctionalList<E> copyOf(final Iterable<? extends E> components) {
        return copyOf(ImmutableList.copyOf(components));
    }

    public static <E> FunctionalList<E> of() {
        return EmptyFunctionalList.instance();
    }

    public static <E> FunctionalList<E> of(final E e0) {
        return new UnrolledList1<>(e0);
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1) {
        return new UnrolledList2<>(ImmutableList.of(e0, e1));
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2) {
        return new UnrolledList3<>(ImmutableList.of(e0, e1, e2));
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3) {
        return new UnrolledList4<>(ImmutableList.of(e0, e1, e2, e3));
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3, final E e4) {
        return new UnrolledList5<>(ImmutableList.of(e0, e1, e2, e3, e4));
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3, final E e4, final E e5) {
        return new UnrolledList6<>(ImmutableList.of(e0, e1, e2, e3, e4, e5));
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3, final E e4, final E e5,
                                           final E e6) {
        return new UnrolledList6<>(ImmutableList.of(e0, e1, e2, e3, e4, e5, e6));
    }

    private static class ImmutableFunctionalListN<E> extends ForwardingImmutableFunctionalList<E> {
        private List<E> delegate;

        public ImmutableFunctionalListN(final Iterable<? extends E> elements) {
            delegate = ImmutableList.copyOf(elements);
        }

        @Override
        protected List<E> delegate() {
            return delegate;
        }
    }
}
