package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.Monitor;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* User: christoph
* Date: 03.05.12
* Time: 12:34
*/
public class SelfUpdatingTree<T> extends ForwardingTwoDimTree<T> {
    private final AtomicBoolean outdated = new AtomicBoolean();
    private final Monitor monitor = new Monitor();
    private final TwoDimTree<T> delegate;
    private final Supplier<Iterable<? extends T>> elements;
    private final Function<? super T, ? extends Location2D> function;

    public SelfUpdatingTree(TwoDimTree<T> delegate, Supplier<Iterable<? extends T>> elements, Function<? super T, ? extends Location2D> function) {
        this.elements = checkNotNull(elements);
        this.function = checkNotNull(function);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    protected TwoDimTree<T> delegate() {
        return delegate;
    }

    @Override
    public void rebuild(Iterable<? extends T> elements, Function<? super T, ? extends Location2D> function) {
        outdated.compareAndSet(false, true);
        monitor.enter();
        try {
            if (outdated.get())
                delegate().rebuild(elements, function);
            outdated.compareAndSet(true, false);
        }
        finally {
            monitor.leave();
        }
    }

    @Override
    public Iterable<T> findObjects(Location2D locatable, double range) {
        rebuildIfOutdated();
        return delegate().findObjects(locatable, range);
    }

    private void rebuildIfOutdated() {
        if (outdated.get())
            rebuild(elements.get(), function);
    }

    @Override
    public Iterator<T> iterator() {
        rebuildIfOutdated();
        return delegate().iterator();
    }

    public void setOutdated() {
        outdated.set(true);
    }
}
