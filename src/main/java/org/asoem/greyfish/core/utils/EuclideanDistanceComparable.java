package org.asoem.greyfish.core.utils;

public interface EuclideanDistanceComparable {

    int dimension();
    double[] getPoint();
    double euclideanDistance(EuclideanDistanceComparable b);
}
