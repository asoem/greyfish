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
            case 2:
                return of(list.get(0), list.get(1));
            case 3:
                return of(list.get(0), list.get(1), list.get(2));
            case 4:
                return of(list.get(0), list.get(1), list.get(2), list.get(3));
            case 5:
                return of(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
            case 6:
                return of(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), list.get(5));
            default:
                return DecoratingFunctionalList.decorate(ImmutableList.copyOf(list));
        }
    }

    public static <E> FunctionalList<E> copyOf(final Iterable<? extends E> components) {
        return copyOf(ImmutableList.copyOf(components));
    }

    public static <E> FunctionalList<E> of() {
        return EmptyFunctionalList.instance();
    }

    public static <E> FunctionalList<E> of(final E e0) {
        return new ImmutableFunctionalList1<>(e0);
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1) {
        return new ImmutableFunctionalList2<>(e0, e1);
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2) {
        return new ImmutableFunctionalList3<>(e0, e1, e2);
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3) {
        return new ImmutableFunctionalList4<>(e0, e1, e2, e3);
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3, final E e4) {
        return new ImmutableFunctionalList5<>(e0, e1, e2, e3, e4);
    }

    public static <E> FunctionalList<E> of(final E e0, final E e1, final E e2, final E e3, final E e4, final E e5) {
        return new ImmutableFunctionalList6<>(e0, e1, e2, e3, e4, e5);
    }
}
