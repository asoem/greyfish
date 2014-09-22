package org.asoem.greyfish.utils.space;

import org.apache.commons.math3.util.MathArrays;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DistanceMeasures {
    private DistanceMeasures() {
    }

    public static EuclideanDistance euclidean() {
        return EuclideanDistance.INSTANCE;
    }

    public static enum EuclideanDistance implements DistanceMeasure<SpatialObject> {
        INSTANCE;

        @Override
        public double apply(final SpatialObject a, final SpatialObject b) {
            checkNotNull(a);
            checkNotNull(b);
            return a.getCentroid().distance(b.getCentroid());
        }

        public double apply(final double[] c1, final double[] c2) {
            checkNotNull(c1);
            checkNotNull(c2);
            return MathArrays.distance(c1, c2);
        }
    }
}
