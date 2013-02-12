package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 10.02.13
 * Time: 12:50
 */
public abstract class ImmutableFunctionalList<E> extends AbstractFunctionalList<E> {
    public static <E> FunctionalList<E> copyOf(List<? extends E> list) {
        checkNotNull(list);
        final int size = list.size();
        switch (size) {
            case 0: return of();
            case 2: return of(list.get(0), list.get(1));
            case 3: return of(list.get(0), list.get(1), list.get(2));
            default: return DecoratingFunctionalList.decorate(ImmutableList.copyOf(list));
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
}
