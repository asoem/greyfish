package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

public class DoubleGene extends AbstractGene<Double> {

    private DoubleGene(DoubleGene doubleGene, CloneMap map) {
        super(doubleGene, map);
    }

	public DoubleGene(Double init, MutationOperator<Double> mutationFunction) {
		super(init, Double.class, mutationFunction);
	}

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new DoubleGene(this, map);
    }

    @Override
    public Gene<Double> mutatedCopy() {
        return new DoubleGene(getMutationFunction().mutate(get()), getMutationFunction());
    }
}
