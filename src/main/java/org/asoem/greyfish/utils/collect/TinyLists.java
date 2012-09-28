package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 21.09.12
 * Time: 15:46
 */
public class TinyLists {

    private static final GenericTinyList<Object> EMPTY_LIST = new GenericTinyList<Object>(ImmutableList.of());

    public static <E> TinyList<E> copyOf(List<E> list) {
        final int size = list.size();
        switch (size) {
            case 2: return new TinyList2<E>(list.get(0), list.get(1));
            default: return new GenericTinyList<E>(list);
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
            case 2: return new TinyList2<E>(function.apply(list.get(0)), function.apply(list.get(1)));
            default: return new GenericTinyList<E>(Iterables.transform(list, function));
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> TinyList<E> of() {
        return (TinyList<E>) EMPTY_LIST;
    }

    private static class GenericTinyList<E> extends ForwardingList<E> implements TinyList<E> {

        private final List<E> delegate;

        private GenericTinyList(Iterable<E> elements) {
            this.delegate = ImmutableList.copyOf(elements);
        }

        @Override
        public E find(Predicate<? super E> predicate) throws NoSuchElementException {
            return Iterables.find(delegate, predicate);
        }

        @Override
        public E find(Predicate<? super E> predicate, E defaultValue) {
            return Iterables.find(delegate, predicate, defaultValue);
        }

        @Override
        protected List<E> delegate() {
            return delegate;
        }
    }
}
