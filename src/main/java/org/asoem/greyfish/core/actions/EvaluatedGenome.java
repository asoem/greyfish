package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.Genome;

public class EvaluatedGenome extends ForwardingGenome {

    private final double fitness;
    private final Genome delegate;

    public EvaluatedGenome(Genome sperm, double fitness) {
        delegate = sperm;
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + fitness + ")";
    }

    @Override
    protected Genome delegate() {
        return delegate;
    }
}
