package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.utils.BitStringUtils;
import org.uncommons.maths.binary.BitString;

public class BitStringGene extends AbstractGene<BitString> {
	
	private double mutationProbability;

	public BitStringGene(final int length, final double mutationProbability) {
		super(new BitString(length));
		this.mutationProbability = mutationProbability;
	}
	
	@Override
	public void mutate() {
		representation = BitStringUtils.mutate(representation, mutationProbability);
	}

	@Override
	public void initialize() {
		representation = BitStringUtils.mutate(representation, 1);
	}

	@Override
	public BitStringGene clone() {
		BitStringGene ret = (BitStringGene) super.clone();
		ret.representation = representation.clone();
		return ret;
	}
}
