// Abstract distance metric class

package org.asoem.sico.kdtree;

abstract class DistanceMetric {
    
    protected abstract double distance(double [] a, double [] b);
}
