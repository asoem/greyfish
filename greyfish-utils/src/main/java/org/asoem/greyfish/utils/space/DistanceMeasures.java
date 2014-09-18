package org.asoem.greyfish.utils.space;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DistanceMeasures {
    private DistanceMeasures() {
    }

    public static DistanceMeasure<SpatialObject> euclidean() {
        return EuclideanDistance.INSTANCE;
    }

    private static enum EuclideanDistance implements DistanceMeasure<SpatialObject> {
        INSTANCE;

        @Override
        public double apply(final SpatialObject a, final SpatialObject b) {
            checkNotNull(a);
            checkNotNull(b);
            return a.getCentroid().distance(b.getCentroid());
        }
    }
}
