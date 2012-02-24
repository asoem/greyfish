package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 10:02
 */
public interface SpatialObject extends Locatable {
    int getDimensions();
    double[] getOrientation();
    double[] getBoundingVolume();
}
