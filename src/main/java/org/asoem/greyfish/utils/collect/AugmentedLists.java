package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.09.12
 * Time: 15:46
 */
public final class AugmentedLists {

    private AugmentedLists() {}

    public static <E> AugmentedList<E> copyOf(List<? extends E> list) {
        checkNotNull(list);
        final int size = list.size();
        switch (size) {
            case 0: return of();
            case 2: return of(list.get(0), list.get(1));
            default: return new ForwardingAugmentedList<E>(ImmutableList.copyOf(list));
        }
    }

    public static <E> AugmentedList<E> copyOf(Iterable<? extends E> components) {
        return copyOf(ImmutableList.copyOf(components));
    }

    public static <E> AugmentedList<E> transform(List<? extends E> list, Function<? super E, E> function) {
        checkNotNull(list);
        checkNotNull(function);
        final int size = list.size();
        switch (size) {
            case 0: return of();
            case 2: return of(function.apply(list.get(0)), function.apply(list.get(1)));
            default: return copyOf(Iterables.transform(list, function));
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> AugmentedList<E> of() {
        return (AugmentedList<E>) EmptyAugmentedList.INSTANCE;
    }

    private static <E> AugmentedList<E> of(E e, E e1) {
        return new TinyAugmentedList2<E>(e, e1);
    }

    public static <E> AugmentedList<E> newAugmentedArrayList(Iterable<? extends E> components) {
        return new ForwardingAugmentedList<E>(Lists.newArrayList(components));
    }

    public static <E> AugmentedList<E> newAugmentedArrayList() {
        return new ForwardingAugmentedList<E>(Lists.<E>newArrayList());
    }
}
