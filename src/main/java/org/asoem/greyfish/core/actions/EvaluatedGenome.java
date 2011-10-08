package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.ForwardingGenome;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;

public class EvaluatedGenome extends ForwardingGenome {

    private final double fitness;
    private final Genome delegate;

    public EvaluatedGenome(Genome sperm, double fitness) {
        delegate = sperm;
        this.fitness = fitness;
    }

    public EvaluatedGenome(EvaluatedGenome genome, DeepCloner cloner) {
        cloner.setAsCloned(genome, this);
        delegate = cloner.cloneField(genome.delegate, Genome.class);
        this.fitness = genome.fitness;
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

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new EvaluatedGenome(this, cloner);
    }
}
