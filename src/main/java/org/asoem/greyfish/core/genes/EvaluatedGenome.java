package org.asoem.greyfish.core.genes;

import org.apache.commons.math.genetics.Fitness;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import static com.google.common.base.Preconditions.checkNotNull;

public class EvaluatedGenome<E extends Gene<?>> extends ForwardingGenome<E> implements Fitness, Comparable<EvaluatedGenome<E>> {

    private final double fitness;

    private final Genome<E> delegate;

    public EvaluatedGenome(Genome<E> sperm, double fitness) {
        delegate = checkNotNull(sperm);
        this.fitness = fitness;
    }

    @SuppressWarnings({"unchecked"}) // cloning is save
    public EvaluatedGenome(EvaluatedGenome<E> genome, DeepCloner cloner) {
        cloner.addClone(this);
        delegate = cloner.cloneField(genome.delegate, Genome.class);
        this.fitness = genome.fitness;
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
    public void updateAllGenes(Genome<? extends E> genes) {
        delegate().updateAllGenes(genes);
    }

    @Override
    public int compareTo(EvaluatedGenome<E> o) {
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

        EvaluatedGenome that = (EvaluatedGenome) o;

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
