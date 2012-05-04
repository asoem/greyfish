package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;

import java.util.Iterator;

/**
 * User: christoph
 * Date: 03.05.12
 * Time: 12:28
 */
public abstract class ForwardingTwoDimTree<T> implements TwoDimTree<T> {

    protected abstract TwoDimTree<T> delegate();

    @Override
    public void rebuild(Iterable<? extends T> elements, Function<? super T, ? extends Location2D> function) {
        delegate().rebuild(elements, function);
    }

    @Override
    public Iterable<T> findObjects(Location2D locatable, double range) {
        return delegate().findObjects(locatable, range);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate().iterator();
    }
}
