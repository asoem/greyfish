package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.DeepCloneable;

public class DoubleGene extends ImmutableGene<Double> {

    private String initialValue = "0.0";
    private String mutationFunction = "0.0";


    public DoubleGene() {
        super(0.0, Double.class, new GeneController<Double>() {
            @Override
            public Double mutate(Double original) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double normalizedDistance(Double orig, Double copy) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double normalizedWeightedDistance(Double orig, Double copy) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Double initialize() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }


    protected DoubleGene(ImmutableGene<Double> doubleImmutableGene, DeepCloner map) {
        super(doubleImmutableGene, map);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DoubleGene(this, cloner);
    }
}
