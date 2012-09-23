package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * User: christoph
 * Date: 21.09.12
 * Time: 15:46
 */
public class TinyLists {
    public static <E> List<E> copyOf(List<E> list) {
        final int size = list.size();
        switch (size) {
            case 2: return new TinyList2<E>(list.get(0), list.get(1));
            default: return ImmutableList.copyOf(list);
        }
    }

    public static <E> List<E> copyOf(Iterable<E> components) {
        if (components instanceof List)
            return copyOf((List<E>) components);
        else
            return copyOf(ImmutableList.copyOf(components));
    }

    public static <E> List<E> transform(List<E> list, Function<? super E, E> function) {
        final int size = list.size();
        switch (size) {
            case 2: return new TinyList2<E>(function.apply(list.get(0)), function.apply(list.get(1)));
            default: return Lists.transform(list, function);
        }
    }

    public static <E> E find(List<E> delegate, Predicate<? super E> predicate) {
        if (delegate instanceof TinyList)
            return ((TinyList<E>) delegate).find(predicate);
        else
            return Iterables.find(delegate, predicate);
    }

    public static <E> E find(List<E> delegate, Predicate<? super E> predicate, E defaultValue) {
        if (delegate instanceof TinyList)
            return ((TinyList<E>) delegate).find(predicate, defaultValue);
        else
            return Iterables.find(delegate, predicate, defaultValue);
    }
}
