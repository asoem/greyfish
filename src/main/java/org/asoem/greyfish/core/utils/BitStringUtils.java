package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.utils.RandomUtils;
import org.uncommons.maths.binary.BitString;

import static org.asoem.greyfish.utils.RandomUtils.trueWithProbability;

public class BitStringUtils {

    /**
     * Creates a new BitString from {@code bs} and flips a random bit with p {@code probability}
     * @param bs The BitString to clone
     * @param p The probability that the new BisString will differ from {@code bs} at a random bit
     * @return The new BisString
     */
	public static BitString mutate(final BitString bs, double p) {
        final BitString ret = bs.clone();
        if (trueWithProbability(p)) {
            ret.flipBit(RandomUtils.nextInt(bs.getLength()));
        }
        return ret;
	}
	
}
