package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.CloneMap;

public class IntegerGene extends AbstractGene<Integer> {

    public IntegerGene(Integer init, MutationOperator<Integer> mutationFunction) {
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
        return new IntegerGene(getMutationFunction().mutate(get()), getMutationFunction());
    }
}
