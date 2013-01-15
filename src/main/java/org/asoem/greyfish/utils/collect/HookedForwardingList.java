package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingListIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 13:53
 */
public abstract class HookedForwardingList<T> extends ForwardingList<T> {

    protected void beforeAddition(@Nullable T element) {}
    protected void afterAddition(@Nullable T element, int index) {}
    protected void beforeRemoval(@Nullable T element) {}
    protected void afterRemoval(@Nullable T element, int index) {}
    protected void beforeReplacement(@Nullable T oldElement, @Nullable T newElement) {}
    protected void afterReplacement(@Nullable T oldElement, @Nullable T newElement) {}

    @Override
    public final boolean add(T element) {
        return standardAdd(element);
    }

    @Override
    public final boolean addAll(@Nonnull Collection<? extends T> ts) {
        return standardAddAll(ts);
    }

    @Override
    public final void add(int index, T element) {
        beforeAddition(element);
        super.add(index, element);
        afterAddition(element, index);
    }

    @Override
    public final boolean addAll(int index, Collection<? extends T> elements) {
        return standardAddAll(index, elements);
    }

    @Override
    public final T remove(int index) {
        Iterator<T> iterator = listIterator(index);
        T ret = iterator.next();
        iterator.remove();
        return ret;
    }

    @Override
    public final boolean removeAll(@Nonnull Collection<?> collection) {
        return standardRemoveAll(collection);
    }

    @Override
    public final boolean remove(Object object) {
        return standardRemove(object);
    }

    @Override
    public final T set(int index, T element) {
        beforeReplacement(get(index), element);
        T ret = super.set(index, element);
        afterReplacement(ret, element);
        return ret;
    }

    @Nonnull
    @Override
    public final ListIterator<T> listIterator() {
        return standardListIterator();
    }

    @Nonnull
    @Override
    public final ListIterator<T> listIterator(final int index) {
        return new ForwardingListIterator<T>() {
            private T current;
            private final ListIterator<T> delegate = HookedForwardingList.this.delegate().listIterator(index);

            @Override
            protected ListIterator<T> delegate() {
                return delegate;
            }

            @Override
            public T next() {
                this.current = super.next();
                return current;
            }

            @Override
            public void remove() {
                beforeRemoval(current);
                super.remove();
                afterRemoval(current, this.previousIndex());
            }
        };
    }

    @Nonnull
    @Override
    public final Iterator<T> iterator() {
        return standardIterator();
    }

    @Override
    public final void clear() {
        standardClear();
    }
}
