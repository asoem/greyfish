package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;

import java.util.Iterator;

public class EvaluatedGenome implements Genome {

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
    public int size() {
        return delegate.size();
    }

    @Override
    public double distance(Genome genome) {
        return delegate.distance(genome);
    }

    @Override
    public Iterable<Gene<?>> findCopiesFor(Iterable<Gene<?>> thisGenes) {
        return delegate.findCopiesFor(thisGenes);
    }

    @Override
    public Genome mutated() {
        return delegate.mutated();
    }

    @Override
    public Genome recombined(Genome genome) {
        return delegate.recombined(genome);
    }

    @Override
    public Iterator<Gene<?>> iterator() {
        return delegate.iterator();
    }
}
