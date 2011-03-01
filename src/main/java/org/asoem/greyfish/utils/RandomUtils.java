package org.asoem.greyfish.utils;

import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import java.util.Random;

public class RandomUtils {

	public static final Random RNG = new MersenneTwisterRNG();

	public static int nextInt() {
		return RNG.nextInt();
	}
	
	public static int nextInt(final int n) {
		return RNG.nextInt(n);
	}

    /**
     * @see java.util.Random#nextDouble()
     * @return the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from this random number generator's sequence
     */
	public static double nextDouble() {
		return RNG.nextDouble();
	}

	public static Random randomInstance() {
		return RNG;
	}

	public static int nextInt(final Integer minIncl, final Integer maxExcl) {
		return minIncl + RNG.nextInt(maxExcl - minIncl);
	}

	public static double nextDouble(double minIncl, double maxExcl) {
		return new ContinuousUniformGenerator(minIncl,maxExcl,RNG).nextValue();
	}
	
	public static float nextFloat(float minIncl, float maxExcl) {
		return new ContinuousUniformGenerator(minIncl,maxExcl,RNG).nextValue().floatValue();
	}

	public static boolean nextBoolean() {
		return RNG.nextBoolean();
	}

	
	/**
	 * @see java.util.Random#nextFloat()
	 */
	public static float nextFloat() {
		return RNG.nextFloat();
	}

    public static boolean trueWithProbability(double probability) {
        return nextDouble() < probability;
    }
}
