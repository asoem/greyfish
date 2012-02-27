package org.asoem.greyfish.core.genes;

import org.apache.commons.math.genetics.Fitness;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

public class EvaluatedChromosome<E extends Gene<?>> extends ForwardingChromosome<E> implements Fitness, Comparable<EvaluatedChromosome<E>> {

    private final double fitness;

    private final Chromosome<E> delegate;

    public EvaluatedChromosome(Chromosome<E> sperm, double fitness) {
        delegate = checkNotNull(sperm);
        this.fitness = fitness;
    }

    @SuppressWarnings({"unchecked"}) // cloning is save
    public EvaluatedChromosome(EvaluatedChromosome<E> genome, DeepCloner cloner) {
        cloner.addClone(this);
        delegate = cloner.cloneField(genome.delegate, Chromosome.class);
        this.fitness = genome.fitness;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + fitness + ")";
    }

    @Override
    protected Chromosome<E> delegate() {
        return delegate;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new EvaluatedChromosome<E>(this, cloner);
    }

    @Override
    public void updateAllGenes(Chromosome<? extends E> genes) {
        delegate().updateAllGenes(genes);
    }

    @Override
    public int compareTo(EvaluatedChromosome<E> o) {
        return Double.compare(this.fitness(), o.fitness());
    }

    @Override
    public double fitness() {
        return fitness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EvaluatedChromosome that = (EvaluatedChromosome) o;

        return Double.compare(that.fitness, fitness) == 0 && delegate.equals(that.delegate);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = fitness != +0.0d ? Double.doubleToLongBits(fitness) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + delegate.hashCode();
        return result;
    }
}
