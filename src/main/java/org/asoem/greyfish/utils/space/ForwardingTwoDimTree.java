package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import org.asoem.greyfish.utils.base.Product2;

/**
 * User: christoph
 * Date: 03.05.12
 * Time: 12:28
 */
public abstract class ForwardingTwoDimTree<T> implements TwoDimTree<T> {

    protected abstract TwoDimTree<T> delegate();

    @Override
    public void rebuild(Iterable<? extends T> elements, Function<? super T, ? extends Product2<Double,Double>> function) {
        delegate().rebuild(elements, function);
    }

    @Override
    public Iterable<T> findObjects(double x, double y, double range) {
        return delegate().findObjects(x, y, range);
    }
}
