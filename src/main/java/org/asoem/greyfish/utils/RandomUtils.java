package org.asoem.greyfish.utils;

import com.google.common.base.Preconditions;
import org.apache.commons.math.random.RandomAdaptor;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.RandomGenerator;

import java.util.Random;

public class RandomUtils {
    public static final RandomGenerator RANDOM_GENERATOR = new org.apache.commons.math.random.MersenneTwister();

    public static final RandomDataImpl RANDOM_DATA = new RandomDataImpl(RANDOM_GENERATOR);

    /**
     * @see java.util.Random#nextDouble()
     * @return the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence
     */
	public static double nextDouble() {
		return RANDOM_GENERATOR.nextDouble();
	}

	public static Random randomInstance() {
		return new RandomAdaptor(RANDOM_GENERATOR);
	}

	public static int nextInt(final Integer minIncl, final Integer maxExcl) {
        Preconditions.checkArgument(maxExcl >= minIncl);
		return minIncl + RANDOM_GENERATOR.nextInt(maxExcl - minIncl);
	}

	public static double nextDouble(double minIncl, double maxExcl) {
        Preconditions.checkArgument(maxExcl >= minIncl);
		return RANDOM_DATA.nextUniform(minIncl, maxExcl);
	}
	
	public static float nextFloat(float minIncl, float maxExcl) {
        Preconditions.checkArgument(maxExcl >= minIncl);
		return (float) RANDOM_DATA.nextUniform(minIncl, maxExcl);
	}

	public static boolean nextBoolean() {
		return RANDOM_GENERATOR.nextBoolean();
	}

    public static boolean trueWithProbability(double probability) {
        if (probability == 0)
            return false;
        else if (probability == 1)
            return true;
        else if (probability > 0 && probability < 1)
            return nextDouble() < probability;
        else
            throw new IllegalArgumentException("Probability not in [0,1]: " + probability);
    }

    public static int nextInt(int size) {
        return RANDOM_GENERATOR.nextInt(size);
    }

    public static double nextDouble(double sum) {
        return nextDouble(0, sum);
    }
}
