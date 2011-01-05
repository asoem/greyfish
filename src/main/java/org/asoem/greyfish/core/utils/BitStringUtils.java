package org.asoem.sico.core.utils;

import org.asoem.sico.utils.RandomUtils;
import org.asoem.sico.utils.SingleElementListDecorator;
import org.uncommons.maths.binary.BitString;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.BitStringMutation;

public class BitStringUtils {

	private final static SingleElementListDecorator<BitString> DECORATOR = new SingleElementListDecorator<BitString>(null);
	
	public static final BitString mutate(final BitString bs, double probability) {
		DECORATOR.set(0, bs);
		final BitStringMutation mutator = new BitStringMutation(new Probability(probability));
		return mutator.apply(DECORATOR, RandomUtils.RNG).get(0);
	}
	
}
