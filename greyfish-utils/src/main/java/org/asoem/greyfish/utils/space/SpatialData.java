package org.asoem.greyfish.utils.space;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import javax.annotation.Nullable;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class SpatialData {
    private SpatialData() {}

    /**
     * Filter all elements out of the given collection that are in the range distance range of given origin. if origin
     * is an element of collection, it will be excluded in the result.
     *
     * @param collection      the collection to filter
     * @param origin          the origin
     * @param range           the range
     * @param distanceMeasure the distance measure to use
     * @param <O>             the type of the objects
     * @return a live view of the given collection
     */
    public static <O> Collection<O> filterNeighbors(
            final Collection<O> collection, final O origin,
            final double range, final DistanceMeasure<? super O> distanceMeasure) {
        return Collections2.filter(collection, new Predicate<O>() {
            @Override
            public boolean apply(@Nullable final O input) {
                checkNotNull(input);
                if (input == origin) {
                    return false;
                }

                final double distance = distanceMeasure.apply(origin, input);
                checkState(distance >= 0, "the distance must be positive");

                return distance <= range;
            }
        });
    }
}
