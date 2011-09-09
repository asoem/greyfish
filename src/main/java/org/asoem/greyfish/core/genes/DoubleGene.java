package org.asoem.greyfish.core.genes;

public class DoubleGene extends ImmutableGene<Double> {
    public DoubleGene(Double element, Class<Double> clazz, GeneController<Double> mutationFunction) {
        super(element, clazz, mutationFunction);
    }

    public DoubleGene(Gene<Double> doubleGene) {
        super(doubleGene);
    }
}
