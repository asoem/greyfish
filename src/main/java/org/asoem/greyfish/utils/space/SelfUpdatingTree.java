package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.asoem.greyfish.utils.base.Product2;

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
    private final Function<? super T, ? extends Product2<Double, Double>> function;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public SelfUpdatingTree(TwoDimTree<T> delegate, Supplier<Iterable<? extends T>> elements, Function<? super T, ? extends Product2<Double, Double>> function) {
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
    public void rebuild(Iterable<? extends T> elements, Function<? super T, ? extends Product2<Double,Double>> function) {
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
    public Iterable<T> findObjects(double x, double y, double range) {
        lock.readLock().lock();
        try {
            rebuildIfOutdated();
            return delegate().findObjects(x, y, range);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void setOutdated() {
        outdated.set(true);
    }
}
