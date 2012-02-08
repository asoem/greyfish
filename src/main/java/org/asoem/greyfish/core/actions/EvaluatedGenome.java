package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.ForwardingGenome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

public class EvaluatedGenome<E extends Gene<?>> extends ForwardingGenome<E> {

    private final double fitness;
    private final Genome<E> delegate;

    public EvaluatedGenome(Genome<E> sperm, double fitness) {
        delegate = sperm;
        this.fitness = fitness;
    }

    @SuppressWarnings({"unchecked"}) // cloning is save
    public EvaluatedGenome(EvaluatedGenome<E> genome, DeepCloner cloner) {
        cloner.addClone(this);
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
    protected Genome<E> delegate() {
        return delegate;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new EvaluatedGenome<E>(this, cloner);
    }

    @Override
    public void replaceGenes(Genome<? extends E> es) {
        delegate().replaceGenes(es);
    }
}
