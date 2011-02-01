package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.utils.BitStringUtils;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.uncommons.maths.binary.BitString;

public class BitStringGene extends AbstractGene<BitString> {
	
	private final double mutationProbability;

	public BitStringGene(final int length, final double mutationProbability) {
		super(new BitString(length));
		this.mutationProbability = mutationProbability;
	}

    private BitStringGene(BitStringGene gene, CloneMap map) {
        super(gene, map);
        this.mutationProbability = gene.mutationProbability;
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
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new BitStringGene(this, map);
    }
}
