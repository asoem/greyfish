package org.asoem.greyfish.utils.collect;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 27.09.12
 * Time: 12:34
 */
public final class SearchableLists {

    private SearchableLists() {}

    public static <E> SearchableList<E> extend(List<E> es) {
        return new SearchableListAdaptor<E>(es);
    }

    private static class SearchableListAdaptor<E> extends ForwardingList<E> implements SearchableList<E> {
        private final List<E> delegate;

        public SearchableListAdaptor(List<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        protected List<E> delegate() {
            return delegate;
        }

        @Override
        public E find(Predicate<? super E> predicate) throws NoSuchElementException {
            return Iterables.find(delegate(), predicate);
        }

        @Override
        public E find(Predicate<? super E> predicate, E defaultValue) {
            return Iterables.find(delegate(), predicate, defaultValue);
        }
    }
}
