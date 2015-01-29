package org.asoem.greyfish.utils.space;

public final class Points {
    private Points() {}

    public static <T extends Point> DistanceMeasure<? super T> euclideanDistance() {
        return EuclideanPointDistance.INSTANCE;
    }

    private enum EuclideanPointDistance implements DistanceMeasure<Point> {
        INSTANCE;

        @Override
        public double apply(final Point a, final Point b) {
            return a.distance(b);
        }
    }
}
