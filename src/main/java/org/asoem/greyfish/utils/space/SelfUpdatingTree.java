package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 03.05.12
 * Time: 12:34
 */
public class SelfUpdatingTree<T> extends ForwardingTwoDimTree<T> {
    private final AtomicBoolean outdated = new AtomicBoolean();
    private final TwoDimTree<T> delegate;
    private final Supplier<Iterable<? extends T>> elements;
    private final Function<? super T, ? extends Location2D> function;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public SelfUpdatingTree(TwoDimTree<T> delegate, Supplier<Iterable<? extends T>> elements, Function<? super T, ? extends Location2D> function) {
        this.elements = checkNotNull(elements);
        this.function = checkNotNull(function);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    protected TwoDimTree<T> delegate() {
        return delegate;
    }

    private void rebuildIfOutdated() {
        if (outdated.get()) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (outdated.get())
                    rebuild(elements.get(), function);
            }
            finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    @Override
    public void rebuild(Iterable<? extends T> elements, Function<? super T, ? extends Location2D> function) {
        lock.writeLock().lock();
        try {
            delegate().rebuild(elements, function);
            outdated.compareAndSet(true, false);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Iterable<T> findObjects(Location2D locatable, double range) {
        lock.readLock().lock();
        try {
            rebuildIfOutdated();
            return delegate().findObjects(locatable, range);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        lock.readLock().lock();
        try {
            rebuildIfOutdated();
            return delegate().iterator();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void setOutdated() {
        outdated.set(true);
    }
}
