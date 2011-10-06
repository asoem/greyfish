package org.asoem.greyfish.utils;

import org.apache.commons.math.random.*;

/**
 * User: christoph
 * Date: 23.09.11
 * Time: 11:41
 *
 * This class provides static access to some methods of an instance of {@link org.apache.commons.math.random.RandomDataImpl}
 * with {@link MersenneTwister} as the {@link RandomGenerator}
 */
public class RandomDataGenerator {

    private static final RandomData RANDOM_DATA_IMPL = new RandomDataImpl(new MersenneTwister(System.currentTimeMillis()));


    public static double gaussian(double mu, double sigma) {
        return RANDOM_DATA_IMPL.nextGaussian(mu, sigma);
    }

    public static double poisson(double mean) {
        return RANDOM_DATA_IMPL.nextPoisson(mean);
    }
}
