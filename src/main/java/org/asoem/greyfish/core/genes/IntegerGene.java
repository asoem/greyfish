package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import org.asoem.greyfish.utils.CloneMap;

public class IntegerGene extends AbstractGene<Integer> {

	public IntegerGene(Integer init, Function<Integer, Integer> mutationFunction) {
		super(init, Integer.class, mutationFunction);
	}

    public IntegerGene(IntegerGene integerGene, CloneMap map) {
        super(integerGene, map);
    }

    @Override
    public IntegerGene deepCloneHelper(CloneMap map) {
        return new IntegerGene(this, map);
    }

    @Override
    public Gene<Integer> mutatedCopy() {
        return new IntegerGene(getMutationFunction().apply(get()), getMutationFunction());
    }
}
