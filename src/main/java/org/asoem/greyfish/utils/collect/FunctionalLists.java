package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.09.12
 * Time: 15:46
 */
public final class FunctionalLists {

    private FunctionalLists() {}

    public static <E> FunctionalList<E> copyOf(List<? extends E> list) {
        checkNotNull(list);
        final int size = list.size();
        switch (size) {
            case 0: return of();
            case 2: return of(list.get(0), list.get(1));
            case 3: return of(list.get(0), list.get(1), list.get(2));
            default: return decorate(ImmutableList.copyOf(list));
        }
    }

    public static <E> FunctionalList<E> copyOf(Iterable<? extends E> components) {
        return copyOf(ImmutableList.copyOf(components));
    }

    @SuppressWarnings("unchecked")
    public static <E> FunctionalList<E> of() {
        return EmptyFunctionalList.instance();
    }

    private static <E> FunctionalList<E> of(E e0, E e1) {
        return new ImmutableFunctionalList2<E>(e0, e1);
    }

    private static <E> FunctionalList<E> of(E e0, E e1, E e2) {
        return new ImmutableFunctionalList3<E>(e0, e1, e2);
    }

    public static <E> FunctionalList<E> decorate(List<E> list) {
        return new ForwardingFunctionalList<E>(list);
    }
}
