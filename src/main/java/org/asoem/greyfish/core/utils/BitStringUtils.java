package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.utils.RandomUtils;
import org.asoem.greyfish.utils.SingleElementListDecorator;
import org.uncommons.maths.binary.BitString;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.BitStringMutation;

public class BitStringUtils {

	private final static SingleElementListDecorator<BitString> DECORATOR = new SingleElementListDecorator<BitString>(null);
	
	public static BitString mutate(final BitString bs, double probability) {
		DECORATOR.set(0, bs);
		final BitStringMutation mutator = new BitStringMutation(new Probability(probability));
		return mutator.apply(DECORATOR, RandomUtils.RNG).get(0);
	}
	
}
