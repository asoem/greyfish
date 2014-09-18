package org.asoem.greyfish.utils.space;

public interface DistanceMeasure<T> {
    /**
     * Calculate the distance between point {@code a} and point {@code b}.
     *
     * @param a the first point
     * @param b the second point
     * @return a positive distance
     */
    double apply(T a, T b);
}
