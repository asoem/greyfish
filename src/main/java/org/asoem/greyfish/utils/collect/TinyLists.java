package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;

/**
 * User: christoph
 * Date: 21.09.12
 * Time: 15:46
 */
public final class TinyLists {

    private TinyLists() {}

    public static <E> TinyList<E> copyOf(List<E> list) {
        final int size = list.size();
        switch (size) {
            case 0: return of();
            case 2: return TinyList2.of(list.get(0), list.get(1));
            default: return new RegularTinyList<E>(list);
        }
    }

    public static <E> TinyList<E> copyOf(Iterable<E> components) {
        if (components instanceof List)
            return copyOf((List<E>) components);
        else
            return copyOf(ImmutableList.copyOf(components));
    }

    public static <E> TinyList<E> transform(List<E> list, Function<? super E, E> function) {
        final int size = list.size();
        switch (size) {
            case 2: return TinyList2.of(function.apply(list.get(0)), function.apply(list.get(1)));
            default: return new RegularTinyList<E>(Iterables.transform(list, function));
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> TinyList<E> of() {
        return (TinyList<E>) EmptyTinyList.INSTANCE;
    }
}
