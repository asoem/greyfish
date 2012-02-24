package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 10:33
 */
public interface Motion {
    int getDimension();
    double getTranslation();
    double[] getRotation();
}
