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
}
