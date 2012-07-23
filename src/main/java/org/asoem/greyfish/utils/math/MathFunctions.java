package org.asoem.greyfish.utils.math;

import org.apache.commons.math3.util.FastMath;

/**
 * User: christoph
 * Date: 23.07.12
 * Time: 16:06
 */
public class MathFunctions {
    public static double gaussian(double x, double norm, double mean, double sigma) {
        return gaussian2(x - mean, norm, 1 / (2 * sigma * sigma));
    }

    public static double gaussian2(double xMinusMean, double norm, double i2s2) {
        return norm * FastMath.exp(-xMinusMean * xMinusMean * i2s2);
    }
}
