package org.asoem.greyfish.utils.space;

import com.google.common.base.Function;
import org.asoem.greyfish.utils.base.Product2;

/**
 * User: christoph
 * Date: 05.10.12
 * Time: 18:00
 */
public interface TwoDimTreeFactory<T> {
    TwoDimTree<T> create(Iterable<? extends T> elements, Function<? super T, ? extends Product2<Double,Double>> coordinates2DFunction);
}
